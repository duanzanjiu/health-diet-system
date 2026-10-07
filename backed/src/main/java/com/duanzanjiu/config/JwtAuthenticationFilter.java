package com.duanzanjiu.config;

import com.duanzanjiu.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 从请求头中获取 jwt token
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token)&&token.startsWith("Bearer ")) {
            String tokenRes = token.substring(7); // 移除 "Bearer " 前缀
            // 验证 token
        try{
            if (jwtUtils.validateToken(tokenRes)) {
                Long userId = jwtUtils.getUserId(tokenRes);
                String username = jwtUtils.getUsername(tokenRes);
                System.out.println(userId);
                System.out.println(username);
                //创建spring security认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
                        (userId,null, Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));
                //确认认证信息存入Spring security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else {
                //token无效或者过期
                //除除旧的认证信息
                SecurityContextHolder.clearContext();
                //返回错误信息
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"token无效或过期\"}");
                return;
            }
        }catch (Exception e){
            //throw new RuntimeException(e);
            SecurityContextHolder.clearContext();
            //返回错误信息
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token认证失败\"}");
            return;
        }


    }
        chain.doFilter(request, response);
}
}