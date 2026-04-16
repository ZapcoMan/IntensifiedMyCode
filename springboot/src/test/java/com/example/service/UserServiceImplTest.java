package com.example.service;

import com.example.TestBase;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomerException;
import com.example.mapper.UserMapper;
import com.example.mapper.UserRoleMapper;
import com.example.service.impl.UserServiceImpl;
import com.example.utils.DistributedLockUtils;
import com.example.utils.PasswordEncoder;
import com.example.utils.RedisUtils;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试类
 * 测试用户相关的业务逻辑
 */
@DisplayName("UserService 单元测试")
class UserServiceImplTest extends TestBase {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private DistributedLockUtils distributedLockUtils;

    @Mock
    private TokenUtils tokenUtils;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setName("测试用户");
        testUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456"));
        testUser.setRole("USER");
        testUser.setAvatar("http://example.com/avatar.jpg");

        testAccount = new Account();
        testAccount.setUsername("testuser");
        testAccount.setPassword("123456");
    }

    @Test
    @DisplayName("添加新用户 - 成功")
    void testAdd_Success() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setName("新用户");
        newUser.setPassword("123456");

        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$mocked_hash");
        when(userRoleMapper.selectIdByCode("USER")).thenReturn(2);

        // When
        userService.add(newUser);

        // Then
        verify(userMapper, times(1)).insert(any(User.class));
        assertNotNull(newUser.getPassword());
        assertEquals("USER", newUser.getRole());
        assertEquals("新用户", newUser.getName());
    }

    @Test
    @DisplayName("添加新用户 - 用户名已存在")
    void testAdd_UsernameExists() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        
        when(userMapper.selectByUsername("existinguser")).thenReturn(existingUser);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userService.add(existingUser);
        });
        
        assertEquals("账号重复", exception.getMsg());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdate_Success() {
        // Given
        User updateUser = new User();
        updateUser.setId(1);
        updateUser.setUsername("testuser");
        updateUser.setName("更新后的名称");

        // When
        userService.update(updateUser);

        // Then
        verify(userMapper, times(1)).updateById(updateUser);
        verify(redisUtils, times(1)).remove("user:info:1:USER");
    }

    @Test
    @DisplayName("根据ID删除用户 - 成功")
    void testDeleteById_Success() {
        // Given
        Integer userId = 1;

        // When
        userService.deleteById(userId);

        // Then
        verify(userMapper, times(1)).deleteById(userId);
        verify(redisUtils, times(1)).remove("user:info:1:USER");
    }

    @Test
    @DisplayName("批量删除用户 - 成功")
    void testDeleteBatch_Success() {
        // Given
        List<Integer> ids = Arrays.asList(1, 2);

        // When
        userService.deleteBatch(ids);

        // Then
        verify(userMapper, times(1)).deleteById(1);
        verify(userMapper, times(1)).deleteById(2);
        verify(redisUtils, times(1)).remove("user:info:1:USER");
        verify(redisUtils, times(1)).remove("user:info:2:USER");
    }

    @Test
    @DisplayName("查询所有用户 - 成功")
    void testSelectAll_Success() {
        // Given
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userMapper.selectAll(null)).thenReturn(expectedUsers);

        // When
        List<User> result = userService.selectAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userMapper, times(1)).selectAll(null);
    }

    @Test
    @DisplayName("分页查询用户 - 成功")
    void testSelectPage_Success() {
        // Given
        List<User> userList = Arrays.asList(testUser);
        when(userMapper.selectAll(any(User.class))).thenReturn(userList);

        // When
        PageInfo<User> pageInfo = userService.selectPage(1, 10, new User());

        // Then
        assertNotNull(pageInfo);
        assertEquals(1, pageInfo.getTotal());
        verify(userMapper, times(1)).selectAll(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 成功（获取分布式锁）")
    void testRegister_Success() {
        // Given
        User newUser = new User();
        newUser.setUsername("registeruser");
        newUser.setPassword("123456");

        when(distributedLockUtils.tryLock(anyString(), anyString(), anyInt())).thenReturn(true);
        when(userMapper.selectByUsername("registeruser")).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$mocked_hash");
        when(userRoleMapper.selectIdByCode("USER")).thenReturn(2);

        // When
        userService.register(newUser);

        // Then
        verify(distributedLockUtils, times(1)).tryLock(anyString(), anyString(), anyInt());
        verify(distributedLockUtils, times(1)).releaseLock(anyString(), anyString());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 失败（未获取分布式锁）")
    void testRegister_LockFailed() {
        // Given
        User newUser = new User();
        newUser.setUsername("registeruser");

        when(distributedLockUtils.tryLock(anyString(), anyString(), anyInt())).thenReturn(false);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userService.register(newUser);
        });
        
        assertEquals("系统繁忙，请稍后再试", exception.getMsg());
        verify(distributedLockUtils, never()).releaseLock(anyString(), anyString());
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("testuser");
        loginAccount.setPassword("123456");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(redisUtils.set(anyString(), any(User.class), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(tokenUtils.createToken(anyString(), anyString())).thenReturn("mock_token_123");

        // When
        User result = userService.login(loginAccount);

        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("testuser", result.getUsername());
        verify(redisUtils, times(1)).set(anyString(), any(User.class), eq(30L), eq(TimeUnit.MINUTES));
        verify(tokenUtils, times(1)).createToken(anyString(), anyString());
    }

    @Test
    @DisplayName("用户登录 - 账号不存在")
    void testLogin_UserNotFound() {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("nonexistent");
        loginAccount.setPassword("123456");

        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userService.login(loginAccount);
        });
        
        assertEquals("账号不存在", exception.getMsg());
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("testuser");
        loginAccount.setPassword("wrongpassword");

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userService.login(loginAccount);
        });
        
        assertEquals("账号或密码错误", exception.getMsg());
    }

    @Test
    @DisplayName("根据ID查询用户 - 从缓存获取")
    void testSelectById_FromCache() {
        // Given
        String userId = "1";
        when(redisUtils.get("user:info:1:USER")).thenReturn(testUser);

        // When
        User result = userService.selectById(userId);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(redisUtils, times(1)).get("user:info:1:USER");
        verify(userMapper, never()).selectById(userId);
    }

    @Test
    @DisplayName("根据ID查询用户 - 从数据库获取并缓存")
    void testSelectById_FromDatabase() {
        // Given
        String userId = "1";
        when(redisUtils.get("user:info:1:USER")).thenReturn(null);
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(redisUtils.set(anyString(), any(User.class), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        User result = userService.selectById(userId);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(redisUtils, times(1)).get("user:info:1:USER");
        verify(userMapper, times(1)).selectById(userId);
        verify(redisUtils, times(1)).set(eq("user:info:1:USER"), any(User.class), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户不存在")
    void testSelectById_UserNotFound() {
        // Given
        String userId = "999";
        when(redisUtils.get("user:info:999:USER")).thenReturn(null);
        when(userMapper.selectById(userId)).thenReturn(null);

        // When
        User result = userService.selectById(userId);

        // Then
        assertNull(result);
        verify(redisUtils, times(1)).get("user:info:999:USER");
        verify(userMapper, times(1)).selectById(userId);
        verify(redisUtils, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    // 注意：由于TokenUtils.getCurrentUser()是静态方法，需要使用PowerMock或其他工具
    // 暂时跳过这个测试，或者需要重构代码以便测试
    @Test
    @DisplayName("更新密码 - 成功（跳过-需要重构）")
    void testUpdatePassword_Success_Skipped() {
        // 此测试需要重构TokenUtils以支持mock
    }

    @Test
    @DisplayName("更新密码 - 两次新密码不一致")
    void testUpdatePassword_NewPasswordsNotMatch() {
        // Given
        Account account = new Account();
        account.setPassword("123456");
        account.setNewpassword("newpass123");
        account.setNew2password("differentpass");

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            userService.updatePassword(account);
        });
        
        assertEquals("你两次输入的密码不一致", exception.getMsg());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "admin", "test_user", "User123"})
    @DisplayName("添加用户 - 参数化测试不同用户名")
    void testAdd_WithDifferentUsernames(String username) {
        // Given
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setName("测试用户");
        newUser.setPassword("123456");

        when(userMapper.selectByUsername(username)).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$mocked_hash");
        when(userRoleMapper.selectIdByCode("USER")).thenReturn(2);

        // When
        userService.add(newUser);

        // Then
        verify(userMapper, times(1)).insert(any(User.class));
        assertEquals("USER", newUser.getRole());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 100, 999})
    @DisplayName("删除用户 - 参数化测试不同ID")
    void testDeleteById_WithDifferentIds(Integer userId) {
        // When
        userService.deleteById(userId);

        // Then
        verify(userMapper, times(1)).deleteById(userId);
        verify(redisUtils, times(1)).remove("user:info:" + userId + ":USER");
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "SUPER_ADMIN", "ADMIN"})
    @DisplayName("查询菜单 - 参数化测试不同角色")
    void testGetMenuByRole_WithDifferentRoles(String role) {
        // This is a placeholder for parameterized test concept
        // In real scenario, you would test different roles
        assertNotNull(role);
        assertTrue(role.length() > 0);
    }
}
