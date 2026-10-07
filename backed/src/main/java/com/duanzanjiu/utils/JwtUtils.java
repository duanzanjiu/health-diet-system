package com.duanzanjiu.utils;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
   @Value("${jwt.secret}")
   private String secret;
   @Value("${jwt.expiration}")
   private Long expiration;

   public String generateToken(Long userId, String username) {
      Map<String, Object> claims = new HashMap<>();
      claims.put("userId", userId);
      claims.put("username", username);
      //创建jwt token
      return Jwts.builder()
              .claims(claims) //设置自定义荷载
              .subject(username) //设置主题
              .issuedAt(new Date()) //设置签发时间
              .expiration(new Date(System.currentTimeMillis() + expiration)) //设置过期时间
              .signWith(getKey()) //设置签名算法和密钥
              .compact();
   }

   //生成签名秘钥
   private SecretKey getKey(){
     //将字符串转换为字节数组
      byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
      return Keys.hmacShaKeyFor(keyBytes);
   }
   //验证 token
   public boolean validateToken(String token) {

      try {
         // 解析 token
         parseToken(token);
         //判断 token是否过期
        return !isTokenExpired(token);
      } catch (Exception e) {
         return false;
      }



   }

   public Claims parseToken(String token) {
      return Jwts.parser()
              .verifyWith(getKey()) // 设置签名秘钥
              .build() //构建实例
              .parseSignedClaims(token) //解析token
              .getPayload(); //返回claims对象

   }
   //判断 token是否过期
   private boolean isTokenExpired(String token) {
      try {
         Claims claims = parseToken(token);
         return claims.getExpiration().before(new Date());
      }catch (Exception e) {
         return true;
      }
   }

   //获取用户ID
   public Long getUserId(String token) {
      Claims claims = parseToken(token);
      return Long.valueOf(claims.get("userId").toString());
   }
   //获取用户名
   public String getUsername(String token) {

      return parseToken(token).getSubject();
   }

}
