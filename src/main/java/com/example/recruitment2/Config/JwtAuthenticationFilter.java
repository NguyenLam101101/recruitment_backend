package com.example.recruitment2.Config;

import com.example.recruitment2.Service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    @Autowired
    JWTProvider jwtProvider;
    @Autowired
    UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException{
        if(request.getRequestURI().startsWith("/employee") ||
                request.getRequestURI().startsWith("/employer") ||
                request.getRequestURI().startsWith("/user")){
            Cookie[] cookies = request.getCookies();
            if(cookies != null){
                String authorization = "";
                for(Cookie cookie: cookies){
                    if (cookie.getName().equals("Authorization"))
                        authorization = cookie.getValue();
                }
                String username;
                if (authorization.startsWith("Bearer%20")) {
                    String token = authorization.substring(9);
                    try {
                        username = jwtProvider.getUsernameFromToken(token);
                        UserDetails userDetails = userService.loadUserByUsername(username);

                        if (username != null && userDetails != null) {
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    } catch (Exception e) {}
                }
            }
        }
        chain.doFilter(request, response);
    }
}
