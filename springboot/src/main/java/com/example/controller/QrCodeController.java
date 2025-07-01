package com.example.controller;

import com.example.common.R;
import com.example.entity.Account;
import com.example.entity.ConfirmDto;
import com.example.service.AdminService;
import com.example.service.QrCodeLoginService;

import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qrcode")
public class QrCodeController {

    @Resource
    private QrCodeLoginService qrCodeLoginService;

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @GetMapping("/token")
    public R generateToken() {
        String token = qrCodeLoginService.generateToken();
        return R.success(token);
    }

    @GetMapping("/status")
    public R getStatus(@RequestParam String token) {
        Account account = qrCodeLoginService.getAccountIfConfirmed(token);
        if (account != null) {
            // 可验证账号有效性并返回完整信息
            Account dbAccount;
            if ("ADMIN".equals(account.getRole())) {
                dbAccount = adminService.login(account);
            } else if ("USER".equals(account.getRole())) {
                dbAccount = userService.login(account);
            } else {
                return R.error("角色错误");
            }
//            return R.ok().put("token", token).put("account", dbAccount);
            return R.ok().data("token", token).data("account", dbAccount);
        }
        return R.ok(); // 尚未扫码
    }

    @PostMapping("/confirm")
    public R confirmLogin(@RequestBody ConfirmDto dto) {
        qrCodeLoginService.confirmLogin(dto.getToken(), dto.getAccount());
        return R.success("扫码成功");
    }


}
