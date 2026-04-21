package com.example.controller;

import com.example.annotation.AuditLogRecord;
import com.example.common.R;
import com.example.common.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.service.AdminService;
import com.example.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin控制器类，负责处理与管理员相关的RESTful API请求
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    /**
     * 注入Admin服务类，用于处理管理员相关的业务逻辑
     */
    @Resource
    AdminService adminService;

    @Resource
    TokenUtils tokenUtils;



    /**
     * 处理管理员用户添加请求的方法
     * <p>
     * 该方法通过接收一个Admin对象作为参数，实现管理员用户的添加操作
     * 使用HTTP POST请求方式，请求体中的数据被直接映射到Admin对象中
     *
     * @param admin 包含管理员用户信息的对象，包括用户名、密码等
     * @return 返回一个R对象，表示操作结果状态
     */
    @Operation(summary = "添加管理员")
    @AuditLogRecord(action = "添加管理员", resource = "管理员")
    @PostMapping("/add")
    public R add(@RequestBody Admin admin) {
        // 调用AdminServiceImpl的add方法，执行管理员用户添加操作
        adminService.add(admin);
        // 返回操作成功的结果状态
        return R.ok();
    }

    /**
     * 更新管理员信息
     * @param admin 前端传入的管理员对象，通过JSON格式传递
     * @return 返回结果对象，表示更新操作是否成功
     */
    @Operation(summary = "更新管理员信息")
    @AuditLogRecord(action = "更新管理员信息", resource = "管理员")
    @PutMapping("/update")
    public R update(@RequestBody Admin admin) {
        adminService.update(admin);
        return R.ok();
    }

    /**
     * 根据ID删除管理员
     * @param id 要删除的管理员的ID，通过URL路径参数传递
     * @return 返回结果对象，表示删除操作是否成功
     */
    @Operation(summary = "根据ID删除管理员")
    @AuditLogRecord(action = "删除管理员", resource = "管理员")
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) {
        adminService.deleteById(id);
        return R.ok();
    }

    /**
     * 批量删除管理员
     * @param list 前端传入的管理员对象列表，通过JSON数组格式传递
     * @return 返回结果对象，表示批量删除操作是否成功
     */
    @Operation(summary = "批量删除管理员")
    @DeleteMapping("/deleteBatch")
    @AuditLogRecord(action = "批量删除管理员", resource = "管理员")
    public R deleteBatch(@RequestBody List<Admin> list) {
        adminService.deleteBatch(list);
        return R.ok();
    }

    /**
     * 验证token是否有效
     *
     * @return 返回token验证结果
     */
    @AuditLogRecord(action = "验证Token", resource = "Token")
    @Operation(summary = "验证Token")
    @GetMapping("/validateToken")
    public R validateToken() {
        Account currentUser = tokenUtils.getCurrentUser();
        if (currentUser != null) {
            return R.ok();
        } else {
            return R.error(ResultCodeEnum.TOKEN_INVALID,"Token已失效或不存在");
        }
    }

    /**
     * 刷新Token（使用RefreshToken换取新的一对Token）
     *
     * @param account 包含userId、role和refreshToken的信息
     * @return 返回新的AccessToken和RefreshToken
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public R refresh(@RequestBody Account account) {
        try {
            String userId = account.getId().toString();
            String role = account.getRole();
            String oldRefreshToken = account.getRefreshToken();
            
            // 1. 验证RefreshToken是否有效
            boolean isValid = tokenUtils.isRefreshTokenValid(userId, role, oldRefreshToken);
            
            if (!isValid) {
                return R.error(ResultCodeEnum.TOKEN_INVALID, "RefreshToken已失效，请重新登录");
            }
            
            // 2. 删除旧的双Token
            tokenUtils.removeTokens(userId, role);
            
            // 3. 生成新的一对Token
            Map<String, String> newTokens = tokenUtils.createTokens(userId, role);
            
            // 4. 返回新的Token对
            return R.success(newTokens);
        } catch (Exception e) {
            return R.error("Token刷新失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有管理员
     * @return 返回结果对象，包含所有管理员信息的列表
     */
    @Operation(summary = "查询所有管理员")
    @AuditLogRecord(action = "查询所有管理员", resource = "管理员")
    @GetMapping("/selectAll")
    public R selectAll() {
        return R.ok().data("adminList", adminService.selectAll());
    }

    /**
     * 分页查询管理员
     * @param pageNum 当前的页码，默认值为1
     * @param pageSize 每页的个数，默认值为10
     * @param admin 管理员对象，可以包含查询条件
     * @return 返回结果对象，包含分页查询结果的PageInfo对象
     */
    @Operation(summary = "分页查询管理员")
    @AuditLogRecord(action = "分页查询管理员", resource = "管理员")
    @GetMapping("/selectPage")
    public R selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             Admin admin) {
        return R.ok().data("pageInfo", adminService.selectPage(pageNum, pageSize, admin));
    }

}
