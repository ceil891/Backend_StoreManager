package org.example.storemanager.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityEvaluator securityEvaluator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtil.isTokenValid(token)) {
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra tài khoản có bị vô hiệu hóa hoặc xóa không
            if (!securityEvaluator.isUserActive(username)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                org.example.storemanager.modules.common.dto.response.ApiResponse<Void> body =
                        org.example.storemanager.modules.common.dto.response.ApiResponse.fail(
                                401,
                                "ACCOUNT_DISABLED",
                                "Tài khoản của bạn đã bị vô hiệu hóa hoặc bị khóa phiên.",
                                request.getRequestURI()
                        );
                mapper.writeValue(response.getOutputStream(), body);
                return;
            }

            java.util.List<String> roles = jwtUtil.extractRoles(token);
            
            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
            if (roles != null && !roles.isEmpty()) {
                for (String role : roles) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        role.startsWith("ROLE_") ? role : "ROLE_" + role
                    ));
                }
            } else {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
            }

            // Set Authentication vào SecurityContext với đầy đủ Authorities
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Lấy token từ header: Authorization: Bearer <token>
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
