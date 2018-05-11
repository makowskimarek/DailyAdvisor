package com.advisor.security;

import com.advisor.model.entity.BlacklistTokenJWT;
import com.advisor.model.entity.User;
import com.advisor.repository.BlacklistTokenJWTRepository;
import com.advisor.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class JWTManager {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BlacklistTokenJWTRepository blacklistTokenJWTRepository;

    private static final String TOKEN_COOKIE_NAME = "_secu";
    private static final String SECRET = "SecretKeyToGenJWTs";
    private static final long EXPIRATION_TIME = 864_000_0000L; // 100 days

    public void jwtLogout(HttpServletRequest req, HttpServletResponse res) {
        Cookie[] cookies = req.getCookies();
        if(cookies == null) {
            return;
        }
        Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_NAME))
                .findFirst()
                .ifPresent(cookie -> {
                    Optional.ofNullable(cookie.getValue()).ifPresent(value -> {
                        Jws<Claims> jws = Jwts.parser()
                                .setSigningKey(SECRET.getBytes())
                                .parseClaimsJws(value);
                        String userName = jws.getBody().getSubject();
                        User user = userRepository.findByEmail(userName);
                        Optional.ofNullable(user).ifPresent(lUser -> {
                            BlacklistTokenJWT jwtBlacklist = new BlacklistTokenJWT();
                            jwtBlacklist.setTokenId(UUID.fromString(jws.getBody().getId()));
                            jwtBlacklist.setUser(lUser);
                            blacklistTokenJWTRepository.save(jwtBlacklist);
                        });
                    });
                    cookie.setMaxAge(0);
                    res.addCookie(cookie);
                });
        return;
    }

    public void jwtLogin(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username = null;
        if(principal instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else {
            username = ((String) principal);
        }
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .setId(UUID.randomUUID().toString())
                .compact();
        Cookie jwtCookie = new Cookie(TOKEN_COOKIE_NAME,token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
    }

    public UsernamePasswordAuthenticationToken authenticateJwt(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if(cookies == null) {
            return null;
        }
        Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_NAME) && cookie.getValue()!=null)
                .findFirst();
        if(jwtCookie.isPresent()) {
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(SECRET.getBytes())
                    .parseClaimsJws(jwtCookie.get().getValue());
            String user = jws.getBody().getSubject();
            if (user != null
                    && !blacklistTokenJWTRepository.existsById(UUID.fromString(jws.getBody().getId()))) {
                        return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
        }
        return null;
    }
}
