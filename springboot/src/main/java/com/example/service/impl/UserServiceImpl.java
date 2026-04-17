package com.example.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomerException;
import com.example.mapper.UserMapper;
import com.example.mapper.UserRoleMapper;
import com.example.service.UserService;
import com.example.utils.DistributedLockUtils;
import com.example.utils.PasswordEncoder;
import com.example.utils.RedisUtils;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;
    @Resource
    RedisUtils redisUtils;
    @Resource
    DistributedLockUtils distributedLockUtils;
    @Resource
    TokenUtils tokenUtils;
    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    PasswordEncoder passwordEncoder;

    /**
     * 添加新用户
     *
     * @param user 待添加的用户信息
     * @throws CustomerException 如果用户名已存在，则抛出异常
     */
    public void add(User user) {
        // 新增：参数校验
        if (StrUtil.isBlank(user.getUsername())) {
            throw new CustomerException("用户名不能为空");
        }
        // 根据新的账号查询数据库  是否存在同样账号的数据
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (dbUser != null) {
            throw new CustomerException("账号重复");
        }
        // BCrypt 加密后存储，与 Admin 保持一致
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            // 明文传入的密码也统一加密存储
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // 如果未设置用户名，则使用账号作为用户名
        if (StrUtil.isBlank(user.getName())) {
            user.setName(user.getUsername());
        }
        // 设置用户角色为普通用户
        user.setRole("USER");
        // 插入用户数据到数据库（密码已在调用处加密，或使用默认加密密码）
        userMapper.insert(user);
        
        // ✅ 插入用户角色关联（RBAC）
        Integer roleId = userRoleMapper.selectIdByCode("USER");
        if (roleId != null && user.getId() != null) {
            userRoleMapper.insert(user.getId(), roleId);
        }
    }

    /**
     * 更新用户信息
     *
     * @param user 需要更新的用户信息
     */
    public void update(User user) {
        // 根据用户ID更新用户信息
        userMapper.updateById(user);
        
        // 清除Redis中的缓存
        String cacheKey = "user:info:" + user.getId() + ":USER";
        redisUtils.remove(cacheKey);
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     */
    public void deleteById(Integer id) {
        // 根据用户ID删除用户
        userMapper.deleteById(id);
        
        // 清除Redis中的缓存
        String cacheKey = "user:info:" + id + ":USER";
        redisUtils.remove(cacheKey);
    }

    /**
     * 批量删除用户
     *
     * @param ids 需要删除的用户ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        // 遍历用户ID列表，逐个删除
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    public List<User> selectAll() {
        // 查询所有用户数据
        return userMapper.selectAll(null);
    }

    /**
     * 分页查询用户
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param user     查询条件
     * @return 分页后的用户列表
     */
    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, User user) {
        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        // 根据条件查询用户数据
        List<User> list = userMapper.selectAll(user);
        // 返回分页信息
        return PageInfo.of(list);
    }

    /**
     * 注册新用户
     *
     * @param user 待注册的用户信息
     * @throws CustomerException 如果用户名已存在，则抛出异常
     */
    public void register(User user) {
        // 使用分布式锁防止重复注册
        String lockKey = "user:register:" + user.getUsername();
        String requestId = String.valueOf(System.currentTimeMillis());
        
        if (distributedLockUtils.tryLock(lockKey, requestId, 10)) {
            try {
                this.add(user);
            } finally {
                // 释放锁
                distributedLockUtils.releaseLock(lockKey, requestId);
            }
        } else {
            throw new CustomerException("系统繁忙，请稍后再试");
        }
    }

    /**
     * 用户登录
     *
     * @param account 用户账号信息
     * @return 登录成功的用户信息
     * @throws CustomerException 如果账号不存在或密码错误，则抛出异常
     */
    public User login(Account account) {
        // 验证账号是否存在
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (dbUser == null) {
            throw new CustomerException("账号不存在");
        }
        // 使用 BCrypt 验证密码
        if (!passwordEncoder.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomerException("账号或密码错误");
        }
        // 创建token并返回给前端
        String token = tokenUtils.createToken(dbUser.getId().toString(), "USER");
        dbUser.setToken(token);
        
        // ✅ 确保role字段正确设置为USER（避免GROUP_CONCAT导致的问题）
        dbUser.setRole("USER");
        
        // 将用户信息缓存到Redis
        String cacheKey = "user:info:" + dbUser.getId() + ":USER";
        redisUtils.set(cacheKey, dbUser, 30, java.util.concurrent.TimeUnit.MINUTES);
        
        // 返回登录成功的用户信息
        return dbUser;
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public User selectById(String id) {
        // 先从Redis获取用户信息
        String cacheKey = "user:info:" + id + ":USER";
        User user = redisUtils.get(cacheKey);
        
        if (user == null) {
            // 如果Redis中没有，则从数据库查询
            user = userMapper.selectById(id);
            if (user != null) {
                // 将用户信息缓存到Redis
                redisUtils.set(cacheKey, user, 30, java.util.concurrent.TimeUnit.MINUTES);
            }
        }
        
        return user;
    }

    /**
     * 更新用户密码
     *
     * @param account 包含原密码和新密码的账号信息
     * @throws CustomerException 如果新密码和旧密码一致或原密码错误，则抛出异常
     */
    public void updatePassword(Account account) {
        //判断新密码和旧密码是否相等
        if(!account.getNewpassword().equals(account.getNewPasswordConfirm())){
            throw  new CustomerException("500","你两次输入的密码不一致");
        }
        // 判断原密码是否正确（使用 BCrypt 验证）
        Account currentUser = tokenUtils.getCurrentUser();
        if (!passwordEncoder.matches(account.getPassword(), currentUser.getPassword())) {
            throw new CustomerException("500", "原密码输入错误");
        }
        // 开始更新密码（BCrypt 加密存储，与 Admin 保持一致）
        User user = userMapper.selectById(currentUser.getId().toString());
        user.setPassword(passwordEncoder.encode(account.getNewpassword()));
        userMapper.updateById(user);
        
        // 清除Redis中的用户信息缓存
        String cacheKey = "user:info:" + currentUser.getId() + ":USER";
        redisUtils.remove(cacheKey);
        
        // 将旧 token 加入黑名单（改密码后旧 token 立即失效）
        tokenUtils.removeToken(currentUser.getId().toString(), "USER");
    }

}