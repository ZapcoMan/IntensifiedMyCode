package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.exception.CustomerException;
import com.example.service.impl.AdminServiceImpl;
import com.example.service.impl.UserServiceImpl;
import jakarta.annotation.Resource;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    AdminServiceImpl adminServiceImpl;
    @Resource
    UserServiceImpl userServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("token");
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter("token");
        }

        if (StrUtil.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Account account = null;
        try {
            String audience = JWT.decode(token).getAudience().get(0);
            String[] split = audience.split("-");
            String userId = split[0];
            String role = split[1];

            if ("ADMIN".equals(role)) {
                account = adminServiceImpl.selectById(userId);
            } else if ("USER".equals(role)) {
                account = userServiceImpl.selectById(userId);
            }

            if (account == null) {
                throw new CustomerException("401", "用户不存在");
            }

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

