package com.cloud.share.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloud.share.handler.GenericResponse;
import com.cloud.share.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {


    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            // so one share and any one can access
        // skipping jwt token
        if(request.getRequestURI().contains("/public")  || request.getRequestURI().contains("/download")){
            filterChain.doFilter(request, response);
            return;
        }


        try {
            String authHeader = request.getHeader("Authorization");

            // Bearer ${token}
            String token = null;
            String username = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token);
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            generateResponseError(response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void generateResponseError(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        Object body = GenericResponse.builder()
                .status("failed")
                .message(e.getMessage())
                .responseStatus(HttpStatus.UNAUTHORIZED)
                .build().create().getBody();
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
