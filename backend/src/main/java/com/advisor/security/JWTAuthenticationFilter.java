package com.advisor.security;

import com.advisor.model.entity.BlacklistTokenJWT;
import com.advisor.model.request.LoginRequest;
import com.advisor.repository.BlacklistTokenJWTRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {



    private AuthenticationManager authenticationManager;

    private JWTManager jwtManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTManager jwtManager) {
        this.authenticationManager = authenticationManager;
        this.jwtManager = jwtManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            LoginRequest creds = new ObjectMapper()
                    .readValue(req.getInputStream(), LoginRequest.class);
            Authentication jwtAuthentication = jwtManager.authenticateJwt(req);
            if(jwtAuthentication != null) {
                return jwtAuthentication;
            }

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new AuthenticationCredentialsNotFoundException("Invalid JSON request.");
        }
    }
}
