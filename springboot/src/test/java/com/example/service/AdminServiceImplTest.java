package com.example.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.TestBase;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomerException;
import com.example.mapper.AdminMapper;
import com.example.service.impl.AdminServiceImpl;
import com.example.utils.RedisUtils;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminServiceImpl 单元测试类
 * 测试管理员相关的业务逻辑
 */
@DisplayName("AdminService 单元测试")
class AdminServiceImplTest extends TestBase {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin testAdmin;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testAdmin = new Admin();
        testAdmin.setId(1);
        testAdmin.setUsername("admin");
        testAdmin.setName("超级管理员");
        testAdmin.setPassword(DigestUtil.md5Hex("123456"));
        testAdmin.setRole("SUPER_ADMIN");
        testAdmin.setAvatar("http://example.com/admin-avatar.jpg");

        testAccount = new Account();
        testAccount.setUsername("admin");
        testAccount.setPassword("123456");
    }

    @Test
    @DisplayName("添加管理员 - 成功")
    void testAdd_Success() {
        // Given
        Admin newAdmin = new Admin();
        newAdmin.setUsername("newadmin");
        newAdmin.setName("新管理员");
        newAdmin.setPassword("123456");

        when(adminMapper.selectByUsername("newadmin")).thenReturn(null);

        // When
        adminService.add(newAdmin);

        // Then
        verify(adminMapper, times(1)).insert(any(Admin.class));
        assertNotNull(newAdmin.getPassword());
        assertEquals("SUPER_ADMIN", newAdmin.getRole());
    }

    @Test
    @DisplayName("添加管理员 - 使用默认密码")
    void testAdd_WithDefaultPassword() {
        // Given
        Admin newAdmin = new Admin();
        newAdmin.setUsername("newadmin");
        newAdmin.setName("新管理员");
        // 不设置密码，应该使用默认密码

        when(adminMapper.selectByUsername("newadmin")).thenReturn(null);

        // When
        adminService.add(newAdmin);

        // Then
        verify(adminMapper, times(1)).insert(any(Admin.class));
        // 密码应该是 MD5Hex 加密后的值
        assertEquals(cn.hutool.crypto.digest.DigestUtil.md5Hex("123456"), newAdmin.getPassword());
        assertEquals("SUPER_ADMIN", newAdmin.getRole());
    }

    @Test
    @DisplayName("添加管理员 - 用户名已存在")
    void testAdd_UsernameExists() {
        // Given
        Admin existingAdmin = new Admin();
        existingAdmin.setUsername("existingadmin");
        
        when(adminMapper.selectByUsername("existingadmin")).thenReturn(existingAdmin);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            adminService.add(existingAdmin);
        });
        
        assertEquals("账号重复", exception.getMsg());
        verify(adminMapper, never()).insert(any(Admin.class));
    }

    @Test
    @DisplayName("更新管理员信息 - 成功")
    void testUpdate_Success() {
        // Given
        Admin updateAdmin = new Admin();
        updateAdmin.setId(1);
        updateAdmin.setUsername("admin");
        updateAdmin.setName("更新后的名称");

        // When
        adminService.update(updateAdmin);

        // Then
        verify(adminMapper, times(1)).updateById(updateAdmin);
        verify(redisUtils, times(1)).remove("user:info:1:SUPER_ADMIN");
    }

    @Test
    @DisplayName("根据ID删除管理员 - 成功")
    void testDeleteById_Success() {
        // Given
        Integer adminId = 1;

        // When
        adminService.deleteById(adminId);

        // Then
        verify(adminMapper, times(1)).deleteById(adminId);
        verify(redisUtils, times(1)).remove("user:info:1:SUPER_ADMIN");
    }

    @Test
    @DisplayName("批量删除管理员 - 成功")
    void testDeleteBatch_Success() {
        // Given
        Admin admin1 = new Admin();
        admin1.setId(1);
        Admin admin2 = new Admin();
        admin2.setId(2);
        List<Admin> adminList = Arrays.asList(admin1, admin2);

        // When
        adminService.deleteBatch(adminList);

        // Then
        verify(adminMapper, times(1)).deleteById(1);
        verify(adminMapper, times(1)).deleteById(2);
        verify(redisUtils, times(1)).remove("user:info:1:SUPER_ADMIN");
        verify(redisUtils, times(1)).remove("user:info:2:SUPER_ADMIN");
    }

    @Test
    @DisplayName("查询所有管理员 - 成功")
    void testSelectAll_Success() {
        // Given
        List<Admin> expectedAdmins = Arrays.asList(testAdmin);
        when(adminMapper.selectAll(null)).thenReturn(expectedAdmins);

        // When
        List<Admin> result = adminService.selectAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
        verify(adminMapper, times(1)).selectAll(null);
    }

    @Test
    @DisplayName("分页查询管理员 - 成功")
    void testSelectPage_Success() {
        // Given
        List<Admin> adminList = Arrays.asList(testAdmin);
        when(adminMapper.selectAll(any(Admin.class))).thenReturn(adminList);

        // When
        PageInfo<Admin> pageInfo = adminService.selectPage(1, 10, new Admin());

        // Then
        assertNotNull(pageInfo);
        assertEquals(1, pageInfo.getTotal());
        verify(adminMapper, times(1)).selectAll(any(Admin.class));
    }

    @Test
    @DisplayName("管理员登录 - 成功")
    void testLogin_Success() {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("admin");
        loginAccount.setPassword("123456");

        when(adminMapper.selectByUsername("admin")).thenReturn(testAdmin);
        when(redisUtils.set(anyString(), any(Admin.class), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        Admin result = adminService.login(loginAccount);

        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("admin", result.getUsername());
        assertEquals("SUPER_ADMIN", result.getRole());
        verify(redisUtils, times(1)).set(anyString(), any(Admin.class), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("管理员登录 - 账号不存在")
    void testLogin_AdminNotFound() {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("nonexistent");
        loginAccount.setPassword("123456");

        when(adminMapper.selectByUsername("nonexistent")).thenReturn(null);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            adminService.login(loginAccount);
        });
        
        assertEquals("账号不存在", exception.getMsg());
    }

    @Test
    @DisplayName("管理员登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("admin");
        loginAccount.setPassword("wrongpassword");

        when(adminMapper.selectByUsername("admin")).thenReturn(testAdmin);

        // When & Then
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            adminService.login(loginAccount);
        });
        
        assertEquals("账号或密码错误", exception.getMsg());
    }

    @Test
    @DisplayName("根据ID查询管理员 - 从缓存获取")
    void testSelectById_FromCache() {
        // Given
        String adminId = "1";
        when(redisUtils.get("user:info:1:SUPER_ADMIN")).thenReturn(testAdmin);

        // When
        Admin result = adminService.selectById(adminId);

        // Then
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        verify(redisUtils, times(1)).get("user:info:1:SUPER_ADMIN");
        verify(adminMapper, never()).selectById(adminId);
    }

    @Test
    @DisplayName("根据ID查询管理员 - 从数据库获取并缓存")
    void testSelectById_FromDatabase() {
        // Given
        String adminId = "1";
        when(redisUtils.get("user:info:1:SUPER_ADMIN")).thenReturn(null);
        when(adminMapper.selectById(adminId)).thenReturn(testAdmin);
        when(redisUtils.set(anyString(), any(Admin.class), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        Admin result = adminService.selectById(adminId);

        // Then
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        verify(redisUtils, times(1)).get("user:info:1:SUPER_ADMIN");
        verify(adminMapper, times(1)).selectById(adminId);
        verify(redisUtils, times(1)).set(eq("user:info:1:SUPER_ADMIN"), any(Admin.class), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("根据ID查询管理员 - 管理员不存在")
    void testSelectById_AdminNotFound() {
        // Given
        String adminId = "999";
        when(redisUtils.get("user:info:999:SUPER_ADMIN")).thenReturn(null);
        when(adminMapper.selectById(adminId)).thenReturn(null);

        // When
        Admin result = adminService.selectById(adminId);

        // Then
        assertNull(result);
        verify(redisUtils, times(1)).get("user:info:999:SUPER_ADMIN");
        verify(adminMapper, times(1)).selectById(adminId);
        verify(redisUtils, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
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
            adminService.updatePassword(account);
        });
        
        assertEquals("你两次输入的密码不一致", exception.getMsg());
    }

    // 注意：由于TokenUtils.getCurrentUser()是静态方法，需要使用PowerMock或其他工具
    // 暂时跳过这个测试，或者需要重构代码以便测试
    @Test
    @DisplayName("更新密码 - 原密码错误（跳过-需要重构）")
    void testUpdatePassword_WrongOldPassword_Skipped() {
        // 此测试需要重构TokenUtils以支持mock
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin1", "super_admin", "manager", "Admin123"})
    @DisplayName("添加管理员 - 参数化测试不同用户名")
    void testAdd_WithDifferentUsernames(String username) {
        // Given
        Admin newAdmin = new Admin();
        newAdmin.setUsername(username);
        newAdmin.setName("测试管理员");
        newAdmin.setPassword("123456");

        when(adminMapper.selectByUsername(username)).thenReturn(null);

        // When
        adminService.add(newAdmin);

        // Then
        verify(adminMapper, times(1)).insert(any(Admin.class));
        assertEquals("SUPER_ADMIN", newAdmin.getRole());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 50, 100})
    @DisplayName("删除管理员 - 参数化测试不同ID")
    void testDeleteById_WithDifferentIds(Integer adminId) {
        // When
        adminService.deleteById(adminId);

        // Then
        verify(adminMapper, times(1)).deleteById(adminId);
        verify(redisUtils, times(1)).remove("user:info:" + adminId + ":SUPER_ADMIN");
    }

    @ParameterizedTest
    @ValueSource(strings = {"password1", "123456", "admin@2024", "StrongP@ss123"})
    @DisplayName("管理员登录 - 参数化测试不同密码格式")
    void testLogin_WithDifferentPasswords(String password) {
        // Given
        Account loginAccount = new Account();
        loginAccount.setUsername("admin");
        loginAccount.setPassword(password);

        Admin mockAdmin = new Admin();
        mockAdmin.setId(1);
        mockAdmin.setUsername("admin");
        mockAdmin.setPassword(cn.hutool.crypto.digest.DigestUtil.md5Hex(password));
        mockAdmin.setRole("SUPER_ADMIN");

        when(adminMapper.selectByUsername("admin")).thenReturn(mockAdmin);
        when(redisUtils.set(anyString(), any(Admin.class), anyLong(), any())).thenReturn(true);

        // When
        Admin result = adminService.login(loginAccount);

        // Then
        assertNotNull(result);
        assertNotNull(result.getToken());
    }
}
