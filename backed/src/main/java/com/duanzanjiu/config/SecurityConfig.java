package com.duanzanjiu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/test",
            "/api/auth/login",
            "/api/common/upload",
            "/uploads/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                //禁用 CSRF （无状态 jwt）
                .csrf(AbstractHttpConfigurer::disable)
                //配置会话管理策略
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //配置请求授权规则
                //对PUBLIC_PATHS路径进行放行，允许所有用户访问（包括未认证用户）
                .authorizeHttpRequests(auth->auth.requestMatchers(PUBLIC_PATHS).permitAll()
                        //其他所有请求都需要认证用户才能访问（jwt token）
                        .anyRequest().authenticated()
                )
                //添加自定义的 jwt 认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                //构建返回实例,包含所有配置的安全过滤规则
        return http.build();
    }

   }
