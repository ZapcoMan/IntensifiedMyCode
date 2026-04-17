package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.enums.RoleEnum;
import com.example.exception.CustomerException;
import com.example.service.AdminService;
import com.example.service.UserService;
import com.example.utils.RedisUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    @Lazy
    private AdminService adminService;

    @Resource
    @Lazy
    private UserService userService;

    @Resource
    private RedisUtils redisUtils;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 白名单列表，这些路径不需要进行token验证
    private final static List<String> WHITE_LIST = Arrays.asList(
            "/login", "/register",
            "/files/upload/", "/files/download/", "/favicon.ico"
    );

    /**
     * 过滤器主要逻辑，对请求进行token验证
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤链，用于将控制权传递给下一个过滤器或目标资源
     * @throws ServletException 如果过滤过程中发生Servlet异常
     * @throws IOException 如果过滤过程中发生IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求URI
        String uri = request.getRequestURI();
        // 检查是否在白名单内（精确匹配或前缀匹配）
        for (String white : WHITE_LIST) {
            if (uri.equals(white) || uri.startsWith(white)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 尝试从请求头获取token
        String token = request.getHeader("token");
        if (StrUtil.isEmpty(token)) {
            // 如果头中没有token，尝试从请求参数中获取
            token = request.getParameter("token");
        }

        // 如果token为空，则直接放行
        if (StrUtil.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 解析token，获取用户ID和角色信息
            String[] split = JWT.decode(token).getAudience().get(0).split("-");
            String userId = split[0];
            String role = split[1];

            // 构建Redis缓存键
            String cacheKey = "user:info:" + userId + ":" + role;

            // 先从Redis获取账户信息
            Account account = redisUtils.get(cacheKey);

            if (account == null) {
                // 如果Redis中没有，则从数据库查询（直接使用 Service 层）
                if (RoleEnum.isAdminRole(role)) {
                    account = adminService.selectById(userId);
                } else if (RoleEnum.isUserRole(role)) {
                    account = userService.selectById(userId);
                }
                
                if (account == null) {
                    throw new CustomerException("401", "用户不存在");
                }

                // 将账户信息缓存到Redis，设置有效期为30分钟
                redisUtils.set(cacheKey, account, 30, java.util.concurrent.TimeUnit.MINUTES);
            }

            // 使用固定的服务端密钥验证token签名
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            verifier.verify(token);

            // 检查token是否在黑名单中（登出或改密码后）
            String tokenKey = "token:user:" + userId + ":" + role;
            String storedToken = redisUtils.get(tokenKey);
            if (storedToken == null || !storedToken.equals(token)) {
                throw new CustomerException("401", "Token已失效，请重新登录");
            }

            // 验证通过，设置Spring Security的认证信息
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(account.getUsername(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (CustomerException e) {
            // 业务异常（如用户不存在、非法角色），返回401错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                "{\"code\":\"%s\",\"message\":\"%s\"}",
                e.getCode() != null ? e.getCode() : "401",
                e.getMsg()
            ));
            return; // 不再继续执行过滤链
        } catch (Exception e) {
            // 其他异常（如Token解析失败、签名验证失败），返回401错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"code\":\"401\",\"message\":\"Token验证失败或已过期\"}"
            );
            return; // 不再继续执行过滤链
        }

        // 验证通过，继续执行过滤链
        filterChain.doFilter(request, response);
    }
}
