package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.enums.RoleEnum;
import com.example.exception.CustomerException;
import com.example.service.AccountService;
import com.example.utils.RedisUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private List<AccountService> accountServices;

    @Resource
    private RedisUtils redisUtils;

    // 服务映射，根据角色代码获取对应的AccountService实现类
    private final Map<String, AccountService> serviceMap = new HashMap<>();

    // 白名单列表，这些路径不需要进行token验证
    private final static List<String> WHITE_LIST = Arrays.asList(
            "/login", "/register",
            "/files/upload", "/files/download", "/favicon.ico"
    );

    /**
     * 初始化serviceMap，将角色代码与AccountService实现类映射
     */
    @PostConstruct
    public void initMap() {
        for (AccountService service : accountServices) {
            serviceMap.put(service.getRole().getCode(), service);
        }
    }

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
        // 检查是否在白名单内
        for (String white : WHITE_LIST) {
            if (uri.startsWith(white)) {
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
                // 如果Redis中没有，则从数据库查询
                AccountService service = serviceMap.get(role);
                if (service == null) throw new CustomerException("401", "非法角色");

                account = service.selectById(userId);
                if (account == null) throw new CustomerException("401", "用户不存在");

                // 将账户信息缓存到Redis，设置有效期为30分钟
                redisUtils.set(cacheKey, account, 30, java.util.concurrent.TimeUnit.MINUTES);
            }

            // 使用账户密码作为密钥验证token
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            verifier.verify(token);

            // 验证通过，设置Spring Security的认证信息
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(account.getUsername(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception ignored) {
            // 任何异常均视为验证失败，直接放行
        }

        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }
}
