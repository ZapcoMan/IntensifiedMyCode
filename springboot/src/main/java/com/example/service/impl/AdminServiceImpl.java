package com.example.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomerException;
import com.example.mapper.AdminMapper;
import com.example.mapper.UserRoleMapper;
import com.example.service.AdminService;
import com.example.utils.PasswordEncoder;
import com.example.utils.RedisUtils;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource AdminMapper adminMapper;
    @Resource
    RedisUtils redisUtils;
    @Resource
    TokenUtils tokenUtils;
    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    PasswordEncoder passwordEncoder;
    
    // 日志对象，用于记录系统日志
    private static final Log log = LogFactory.getLog(AdminServiceImpl.class);

    /**
     * 添加管理员账户
     *
     * @param admin 待添加的管理员对象，包含用户名和密码等信息
     * @throws CustomerException 如果用户名已存在，则抛出此异常
     */
    public void add(Admin admin) {
        // 根据新的账号查询数据库  是否存在同样账号的数据
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (dbAdmin != null) {
            throw new CustomerException("账号重复");
        }
        // BCrypt 加密后存储，与 User 保持一致
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("123456"));
        } else {
            // 明文传入的密码也统一加密存储
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        admin.setRole("SUPER_ADMIN");
        adminMapper.insert(admin);
        
        // ✅ 插入用户角色关联（RBAC）
        Integer roleId = userRoleMapper.selectIdByCode("SUPER_ADMIN");
        if (roleId != null && admin.getId() != null) {
            userRoleMapper.insert(admin.getId(), roleId);
        }
    }

    /**
     * 更新管理员信息
     *
     * @param admin 包含更新信息的管理员对象
     */
    public void update(Admin admin) {
        adminMapper.updateById(admin);
        
        // 清除Redis中的缓存
        String cacheKey = "user:info:" + admin.getId() + ":SUPER_ADMIN";
        redisUtils.remove(cacheKey);
    }

    /**
     * 根据ID删除管理员账户
     *
     * @param id 要删除的管理员账户ID
     */
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
        
        // 清除Redis中的缓存
        String cacheKey = "user:info:" + id + ":SUPER_ADMIN";
        redisUtils.remove(cacheKey);
    }

    /**
     * 批量删除管理员账户
     *
     * @param list 包含多个待删除的管理员对象的列表
     */
    public void deleteBatch(List<Admin> list) {
        for (Admin admin : list) {
            this.deleteById(admin.getId());
        }
    }

    /**
     * 根据ID查询管理员信息
     *
     * @param id 要查询的管理员账户ID
     * @return 查询到的管理员对象
     */
    public Admin selectById(String id) {
        // 先从Redis获取用户信息
        String cacheKey = "user:info:" + id + ":SUPER_ADMIN";
        Admin admin = redisUtils.get(cacheKey);
        
        if (admin == null) {
            // 如果Redis中没有，则从数据库查询
            admin = adminMapper.selectById(id);
            if (admin != null) {
                // 将用户信息缓存到Redis
                redisUtils.set(cacheKey, admin, 30, java.util.concurrent.TimeUnit.MINUTES);
            }
        }
        
        return admin;
    }

    /**
     * 查询所有管理员信息
     *
     * @return 包含所有管理员对象的列表
     */
    public List<Admin> selectAll() {
        return adminMapper.selectAll(null);
    }

    /**
     * 分页查询管理员信息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param admin    用于查询的管理员对象，可以为null
     * @return 包含分页信息的PageInfo对象
     */
    public PageInfo<Admin> selectPage(Integer pageNum, Integer pageSize, Admin admin) {
        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    /**
     * 管理员登录方法
     *
     * @param account 包含用户名和密码的账户信息
     * @return 登录成功的管理员对象（包含双Token）
     * @throws CustomerException 如果账号不存在或密码错误，则抛出此异常
     */
    public Admin login(Account account) {
        // 验证账号是否存在
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (dbAdmin == null) {
            throw new CustomerException("账号不存在");
        }

        // 使用 BCrypt 验证密码
        boolean isValid = passwordEncoder.matches(account.getPassword(), dbAdmin.getPassword());

        // 如果密码不正确，抛出异常
        if (!isValid) {
            log.warn("管理员登录失败: username=" + account.getUsername());
            throw new CustomerException("账号或密码错误");
        }

        // ✅ 生成双Token（AccessToken + RefreshToken）
        Map<String, String> tokens = tokenUtils.createTokens(dbAdmin.getId().toString(), "SUPER_ADMIN");
        dbAdmin.setToken(tokens.get("accessToken")); // 主token设为AccessToken
        dbAdmin.setRefreshToken(tokens.get("refreshToken")); // 新增字段存储RefreshToken
        
        // ✅ 确保role字段正确设置为SUPER_ADMIN（避免GROUP_CONCAT导致的问题）
        dbAdmin.setRole("SUPER_ADMIN");
        
        // 将用户信息缓存到Redis（延长缓存时间至1小时）
        String cacheKey = "user:info:" + dbAdmin.getId() + ":SUPER_ADMIN";
        redisUtils.set(cacheKey, dbAdmin, 1, java.util.concurrent.TimeUnit.HOURS);

        // 返回登录成功的管理员对象
        return dbAdmin;
    }

    /**
     * 更新用户密码
     * 此方法首先检查用户输入的新密码和确认密码是否一致，然后验证当前输入的原密码是否正确，
     * 最后在验证通过后更新数据库中的密码信息
     *
     * @param account 包含用户输入的原密码、新密码和确认密码的账户对象
     * @throws CustomerException 如果新密码和确认密码不一致，或者原密码错误，抛出自定义异常
     */
    public void updatePassword(Account account) {
        //判断新密码和确认密码是否相等
        if(!account.getNewpassword().equals(account.getNewPasswordConfirm())){
            throw  new CustomerException("500","你两次输入的密码不一致");
        }
        //判断原密码是否正确（使用 BCrypt 验证）
        Account currentUser = tokenUtils.getCurrentUser();
        if(!passwordEncoder.matches(account.getPassword(), currentUser.getPassword())){
            throw new CustomerException("500", "原密码输入错误");
        }
        //开始更新密码（BCrypt 加密存储）
        String newPasswordHash = passwordEncoder.encode(account.getNewpassword());
        Admin admin = adminMapper.selectById(currentUser.getId().toString());
        admin.setPassword(newPasswordHash);
        adminMapper.updateById(admin);
        
        // 清除Redis中的用户信息缓存
        String cacheKey = "user:info:" + currentUser.getId() + ":SUPER_ADMIN";
        redisUtils.remove(cacheKey);
        
        // 将旧 token 加入黑名单（改密码后旧 token 立即失效）
        tokenUtils.removeTokens(currentUser.getId().toString(), "SUPER_ADMIN");
    }
}