package aichat.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    private static final String SECRET_KEY = "dvom[]{}392ms.scdv00-scsdom"; // 请更改为您的密钥
    private static final long EXPIRATION_TIME = 86400000*7; // 7天

    public static String createJWT(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public static void setJwtCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("JWT_TOKEN", jwt);
        cookie.setHttpOnly(true); // 设置 HttpOnly 属性
        cookie.setSecure(false); // 如果使用 HTTPS，请设置为 true
        cookie.setPath("/"); // 设置 cookie 的路径
        cookie.setMaxAge((int) (EXPIRATION_TIME / 1000)); // 设置过期时间
        response.addCookie(cookie);
    }

    public static Claims validateJWT(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
