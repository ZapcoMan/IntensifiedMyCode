package com.example.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomerException;
import com.example.mapper.AdminMapper;
import com.example.service.impl.AdminServiceImpl;
import com.example.utils.RedisUtils;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminServiceImpl 单元测试
 * 使用 @SpringBootTest 启动真实 Spring 上下文
 * 使用 @Transactional 让每个测试方法结束后自动回滚，不污染数据库
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminServiceImpl adminService;

    @Autowired
    private AdminMapper adminMapper;

    @MockBean
    private RedisUtils redisUtils;

    @MockBean
    private TokenUtils tokenUtils;

    @BeforeEach
    void setUp() {
        // 让 Redis 的 get() 默认返回 null（强制走数据库）
        when(redisUtils.get(anyString())).thenReturn(null);
        when(redisUtils.set(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(redisUtils.remove(anyString())).thenReturn(true);
        // Mock TokenUtils.createToken 返回固定 token
        when(tokenUtils.createToken(anyString(), anyString())).thenReturn("mock-jwt-token");
    }

    // ========== CRUD 基础测试 ==========

    @Test
    void add_用户名重复_抛异常() {
        // given: admin1 在 V1 迁移脚本中已存在
        Admin admin = new Admin();
        admin.setUsername("admin1");
        admin.setPassword("123456");
        admin.setName("重复测试");

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () -> adminService.add(admin));
        assertEquals("账号重复", ex.getMessage());
    }

    @Test
    void add_新管理员_密码为空时默认123456() {
        // given: 新用户名
        Admin admin = new Admin();
        admin.setUsername("testAddAdmin");
        admin.setName("测试添加");

        // when
        adminService.add(admin);

        // then: 密码应为 MD5Hex("123456")
        Admin dbAdmin = adminMapper.selectByUsername("testAddAdmin");
        assertNotNull(dbAdmin);
        assertEquals(DigestUtil.md5Hex("123456"), dbAdmin.getPassword());
        assertEquals("SUPER_ADMIN", dbAdmin.getRole());
    }

    @Test
    void add_新管理员_明文密码自动加密() {
        // given
        Admin admin = new Admin();
        admin.setUsername("testAddAdmin2");
        admin.setPassword("myPassword123");  // 明文传入
        admin.setName("测试加密");

        // when
        adminService.add(admin);

        // then: 密码应为 MD5Hex 加密后的值
        Admin dbAdmin = adminMapper.selectByUsername("testAddAdmin2");
        assertNotNull(dbAdmin);
        assertEquals(DigestUtil.md5Hex("myPassword123"), dbAdmin.getPassword());
    }

    @Test
    void selectById_从数据库查询_缓存未命中() {
        // given: Redis 返回 null（由 @BeforeEach 配置）
        // when
        Admin admin = adminService.selectById("1");

        // then
        assertNotNull(admin);
        assertEquals("admin1", admin.getUsername());
        // 验证缓存被设置
        verify(redisUtils).set(eq("user:info:1:SUPER_ADMIN"), any(Admin.class), eq(30L), any());
    }

    @Test
    void selectById_从Redis缓存命中_不查数据库() {
        // given: Redis 有缓存
        Admin cachedAdmin = new Admin();
        cachedAdmin.setId(1);
        cachedAdmin.setUsername("admin1-from-cache");
        when(redisUtils.get("user:info:1:SUPER_ADMIN")).thenReturn(cachedAdmin);

        // when
        Admin result = adminService.selectById("1");

        // then
        assertEquals("admin1-from-cache", result.getUsername());
        // 不应该调用 Mapper（走缓存）
        verify(adminMapper, never()).selectById(anyString());
    }

    @Test
    void selectAll_正常查询() {
        // when
        List<Admin> result = adminService.selectAll();

        // then: V1 迁移脚本插入了 admin1
        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test
    void selectPage_正常分页() {
        // when
        var pageInfo = adminService.selectPage(1, 5, new Admin());

        // then
        assertNotNull(pageInfo);
        assertEquals(5, pageInfo.getPageSize());
        assertEquals(1, pageInfo.getPageNum());
    }

    @Test
    void update_正常更新_清除Redis缓存() {
        // given: 先插入一个管理员
        Admin admin = new Admin();
        admin.setUsername("testUpdate");
        admin.setName("更新前");
        admin.setPassword("123456");
        adminService.add(admin);
        Admin dbAdmin = adminMapper.selectByUsername("testUpdate");

        // when: 更新名称
        dbAdmin.setName("更新后");
        adminService.update(dbAdmin);

        // then
        Admin updated = adminMapper.selectByUsername("testUpdate");
        assertEquals("更新后", updated.getName());
        // 验证缓存被清除
        verify(redisUtils).remove("user:info:" + dbAdmin.getId() + ":SUPER_ADMIN");
    }

    @Test
    void deleteById_正常删除_清除Redis缓存() {
        // given: 先插入
        Admin admin = new Admin();
        admin.setUsername("testDelete");
        admin.setPassword("123456");
        adminService.add(admin);
        Admin dbAdmin = adminMapper.selectByUsername("testDelete");

        // when
        adminService.deleteById(dbAdmin.getId());

        // then: 数据库中已不存在
        assertNull(adminMapper.selectByUsername("testDelete"));
        // 验证缓存被清除
        verify(redisUtils).remove("user:info:" + dbAdmin.getId() + ":SUPER_ADMIN");
    }

    @Test
    void deleteBatch_批量删除() {
        // given: 先插入两个
        Admin admin1 = new Admin();
        admin1.setUsername("testBatch1");
        admin1.setPassword("123456");
        adminService.add(admin1);

        Admin admin2 = new Admin();
        admin2.setUsername("testBatch2");
        admin2.setPassword("123456");
        adminService.add(admin2);

        Admin dbAdmin1 = adminMapper.selectByUsername("testBatch1");
        Admin dbAdmin2 = adminMapper.selectByUsername("testBatch2");
        List<Admin> batch = Arrays.asList(dbAdmin1, dbAdmin2);

        // when
        adminService.deleteBatch(batch);

        // then
        assertNull(adminMapper.selectByUsername("testBatch1"));
        assertNull(adminMapper.selectByUsername("testBatch2"));
    }

    // ========== 登录测试 ==========

    @Test
    void login_成功_返回token() {
        // given: V1 脚本中 admin1 密码为 123456（MD5Hex 存储）
        Account account = new Account();
        account.setUsername("admin1");
        account.setPassword("123456");

        // when
        Admin result = adminService.login(account);

        // then
        assertNotNull(result);
        assertEquals("admin1", result.getUsername());
        assertNotNull(result.getToken());
        // 验证 Redis 被调用
        verify(redisUtils).set(eq("user:info:1:SUPER_ADMIN"), any(Admin.class), eq(30L), any());
    }

    @Test
    void login_账号不存在_抛异常() {
        // given
        Account account = new Account();
        account.setUsername("nonExistentUser");
        account.setPassword("123456");

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () -> adminService.login(account));
        assertEquals("账号不存在", ex.getMessage());
    }

    @Test
    void login_密码错误_抛异常() {
        // given
        Account account = new Account();
        account.setUsername("admin1");
        account.setPassword("wrongPassword");

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () -> adminService.login(account));
        assertEquals("账号或密码错误", ex.getMessage());
    }

    @Test
    void login_密码为空_抛异常() {
        // given
        Account account = new Account();
        account.setUsername("admin1");
        account.setPassword("");

        // when & then
        assertThrows(CustomerException.class, () -> adminService.login(account));
    }
}
