package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.exception.CustomerException;
import com.example.service.AccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 用于存储 role -> 对应的 AccountService 实现类
    private final Map<String, AccountService> serviceMap = new HashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 从请求头或请求参数中获取 token
        String token = request.getHeader("token");
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter("token");
        }

        // 如果 token 为空，则继续执行过滤器链并返回
        if (StrUtil.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Account account = null;
        try {
            // 解析 token 获取 audience 字段，并分割出用户ID和角色
            String audience = JWT.decode(token).getAudience().get(0);
            String[] split = audience.split("-");
            String userId = split[0];
            String role = split[1];

            // 根据角色获取对应的 AccountService 实现类
            AccountService service = serviceMap.get(role);
            if (service == null) throw new CustomerException("401", "不支持的角色");

            // 使用 AccountService 查询用户信息
            account = service.selectById(userId);

            // 如果用户不存在，抛出异常
            if (account == null) {
                throw new CustomerException("401", "用户不存在");
            }

            // 创建 JWT 验证器，验证 token 的签名
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            verifier.verify(token);

            // 再次检查用户是否存在（防止在验证过程中用户被删除）
            if (account == null) {
                throw new CustomerException("401", "用户不存在");
            }

            // 再次创建 JWT 验证器，验证 token 的签名（可能是为了确保验证过程的可靠性）
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            jwtVerifier.verify(token);

            // ✅ 设置认证信息进 SecurityContext
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(account.getUsername(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            // token 无效，不设置认证，继续走匿名流程
        }

        filterChain.doFilter(request, response);

    }
}

