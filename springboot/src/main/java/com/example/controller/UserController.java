package com.example.controller;

import com.example.annotation.AuditLogRecord;
import com.example.common.R;

import com.example.common.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器类，处理与用户相关的RESTful API请求
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 注入用户服务层接口，用于执行用户相关的业务逻辑
     */
    @Resource
    UserService userService;

    @Resource
    TokenUtils tokenUtils;

    /**
     * 添加新用户
     *
     * @param user 待添加的用户对象，通过请求体接收
     * @return 返回添加操作的结果
     */
    @Operation(summary = "添加新用户")
    @AuditLogRecord(action = "添加新用户", resource = "用户")
    @PostMapping("/add")
    public R add(@RequestBody User user) {
        userService.add(user);
        return R.ok();
    }

    /**
     * 更新用户信息
     *
     * @param user 待更新的用户对象，通过请求体接收
     * @return 返回更新操作的结果
     */
    @AuditLogRecord(action = "更新用户信息", resource = "用户")
    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public R update(@RequestBody User user) {
        userService.update(user);
        return  R.ok();
    }

    /**
     * 根据用户ID删除用户
     *
     * @param id 用户ID，通过URL参数接收
     * @return 返回删除操作的结果
     */

    @AuditLogRecord(action = "删除用户", resource = "用户")
    @Operation(summary = "根据用户ID删除用户")
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) {
        userService.deleteById(id);
        return  R.ok();
    }

    /**
     * 批量删除用户
     *
     * @param ids 待删除的用户ID列表，通过请求体接收
     * @return 返回批量删除操作的结果
     */
    @AuditLogRecord(action = "批量删除用户", resource = "用户")
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/deleteBatch")
    public R deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return  R.ok();
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
     * 查询所有用户信息
     *
     * @return 返回所有用户信息列表
     */
    @AuditLogRecord(action = "查询所有用户", resource = "用户")
    @Operation(summary = "查询所有用户")
    @GetMapping("/selectAll")
    public R selectAll() {
        List<User> userList = userService.selectAll();
        return  R.ok().data("userList", userList);
    }

    /**
     * 分页查询用户信息
     *
     * @param pageNum 页码，默认值为1
     * @param pageSize 每页记录数，默认值为10
     * @param user 用户对象，用于模糊查询
     * @return 返回分页查询结果，包含用户信息
     */
    @AuditLogRecord(action = "分页查询用户信息", resource = "用户")
    @Operation(summary = "分页查询用户信息")
    @GetMapping("/selectPage")
    public R selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             User user) {
        return  R.ok().data("pageInfo", userService.selectPage(pageNum, pageSize, user));  // 返回的是分页的对象
    }

}
