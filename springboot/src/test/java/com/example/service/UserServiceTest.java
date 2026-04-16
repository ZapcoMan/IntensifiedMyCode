package com.example.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomerException;
import com.example.mapper.UserMapper;
import com.example.service.impl.UserServiceImpl;
import com.example.utils.DistributedLockUtils;
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
 * UserServiceImpl 单元测试
 * 与 AdminServiceTest 类似，但重点测试：
 * 1. 密码统一使用 MD5Hex 加密
 * 2. 登录逻辑（与 Admin 的差异）
 * 3. 分布式锁防并发注册
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserMapper userMapper;

    @MockBean
    private RedisUtils redisUtils;

    @MockBean
    private DistributedLockUtils distributedLockUtils;

    @MockBean
    private TokenUtils tokenUtils;

    @BeforeEach
    void setUp() {
        // Redis 默认返回 null（走数据库）
        when(redisUtils.get(anyString())).thenReturn(null);
        when(redisUtils.set(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(redisUtils.remove(anyString())).thenReturn(true);
        // 分布式锁默认成功
        when(distributedLockUtils.tryLock(anyString(), anyString(), anyInt())).thenReturn(true);
        when(distributedLockUtils.releaseLock(anyString(), anyString())).thenReturn(true);
        // TokenUtils 默认返回 mock token
        when(tokenUtils.createToken(anyString(), anyString())).thenReturn("mock-user-token");
    }

    // ========== CRUD 测试 ==========

    @Test
    void add_新用户_密码自动MD5Hex加密() {
        // given
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("plainPassword");  // 明文传入
        user.setName("测试用户");

        // when
        userService.add(user);

        // then: 密码应为 MD5Hex 加密后的值
        User dbUser = userMapper.selectByUsername("testUser");
        assertNotNull(dbUser);
        assertEquals(DigestUtil.md5Hex("plainPassword"), dbUser.getPassword());
        assertEquals("USER", dbUser.getRole());
    }

    @Test
    void add_新用户_密码为空默认123456() {
        // given
        User user = new User();
        user.setUsername("testUserNoPwd");
        user.setName("无密码用户");

        // when
        userService.add(user);

        // then: 默认密码 123456 也应 MD5Hex 加密
        User dbUser = userMapper.selectByUsername("testUserNoPwd");
        assertEquals(DigestUtil.md5Hex("123456"), dbUser.getPassword());
    }

    @Test
    void add_用户名重复_抛异常() {
        // given: 先插入一个
        userService.add(User.builder().username("dupUser").password("123456").name("dup").build());

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () ->
                userService.add(User.builder().username("dupUser").password("123456").name("dup2").build())
        );
        assertEquals("账号重复", ex.getMessage());
    }

    @Test
    void selectAll_正常查询() {
        // when
        List<User> result = userService.selectAll();
        assertNotNull(result);
    }

    @Test
    void selectPage_正常分页() {
        // when
        var pageInfo = userService.selectPage(1, 5, new User());
        assertEquals(5, pageInfo.getPageSize());
        assertEquals(1, pageInfo.getPageNum());
    }

    @Test
    void update_正常更新_清除Redis缓存() {
        // given
        userService.add(User.builder().username("testUpdateUser").password("123456").name("原名").build());
        User dbUser = userMapper.selectByUsername("testUpdateUser");

        // when
        dbUser.setName("新名字");
        userService.update(dbUser);

        // then
        User updated = userMapper.selectByUsername("testUpdateUser");
        assertEquals("新名字", updated.getName());
        verify(redisUtils).remove("user:info:" + dbUser.getId() + ":USER");
    }

    @Test
    void deleteById_正常删除_清除Redis缓存() {
        // given
        userService.add(User.builder().username("testDelUser").password("123456").name("删除测试").build());
        User dbUser = userMapper.selectByUsername("testDelUser");

        // when
        userService.deleteById(dbUser.getId());

        // then
        assertNull(userMapper.selectByUsername("testDelUser"));
        verify(redisUtils).remove("user:info:" + dbUser.getId() + ":USER");
    }

    @Test
    void deleteBatch_批量删除() {
        // given
        userService.add(User.builder().username("batch1").password("123456").name("b1").build());
        userService.add(User.builder().username("batch2").password("123456").name("b2").build());
        User u1 = userMapper.selectByUsername("batch1");
        User u2 = userMapper.selectByUsername("batch2");

        // when
        userService.deleteBatch(Arrays.asList(u1, u2));

        // then
        assertNull(userMapper.selectByUsername("batch1"));
        assertNull(userMapper.selectByUsername("batch2"));
    }

    // ========== 登录测试（与 Admin 对比：密码也是 MD5Hex） ==========

    @Test
    void login_成功_返回token() {
        // given: 先添加用户（密码自动 MD5Hex 加密）
        userService.add(User.builder().username("loginUser").password("123456").name("登录测试").build());
        Account account = new Account();
        account.setUsername("loginUser");
        account.setPassword("123456");  // 明文

        // when
        User result = userService.login(account);

        // then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("loginUser", result.getUsername());
    }

    @Test
    void login_账号不存在_抛异常() {
        // given
        Account account = new Account();
        account.setUsername("nonExistent");
        account.setPassword("123456");

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () -> userService.login(account));
        assertEquals("账号不存在", ex.getMessage());
    }

    @Test
    void login_密码错误_抛异常() {
        // given
        userService.add(User.builder().username("wrongPwdUser").password("123456").name("错误密码").build());
        Account account = new Account();
        account.setUsername("wrongPwdUser");
        account.setPassword("wrongPassword");  // 错误密码

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () -> userService.login(account));
        assertEquals("账号或密码错误", ex.getMessage());
    }

    // ========== 注册测试（分布式锁） ==========

    @Test
    void register_成功_获取分布式锁后添加用户() {
        // given: 分布式锁可用
        when(distributedLockUtils.tryLock(eq("user:register:newRegUser"), anyString(), eq(10))).thenReturn(true);

        // when
        userService.register(User.builder().username("newRegUser").password("123456").name("注册测试").build());

        // then
        assertNotNull(userMapper.selectByUsername("newRegUser"));
        verify(distributedLockUtils).tryLock("user:register:newRegUser", anyString(), eq(10));
        verify(distributedLockUtils).releaseLock("user:register:newRegUser", anyString());
    }

    @Test
    void register_锁获取失败_抛异常() {
        // given: 分布式锁被占用
        when(distributedLockUtils.tryLock(anyString(), anyString(), anyInt())).thenReturn(false);

        // when & then
        CustomerException ex = assertThrows(CustomerException.class, () ->
                userService.register(User.builder().username("busyUser").password("123456").name("并发测试").build())
        );
        assertEquals("系统繁忙，请稍后再试", ex.getMessage());
    }

    @Test
    void selectById_缓存命中_不查数据库() {
        // given
        User cachedUser = new User();
        cachedUser.setId(999);
        cachedUser.setUsername("cachedUser");
        when(redisUtils.get("user:info:999:USER")).thenReturn(cachedUser);

        // when
        User result = userService.selectById("999");

        // then
        assertEquals("cachedUser", result.getUsername());
        verify(userMapper, never()).selectById(anyString());
    }
}
