package com.creatorops.auth.security;

import com.creatorops.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;
import com.creatorops.auth.entity.User;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = jwtService.extractUserId(jwt);
                String name = jwtService.extractName(jwt);
                String role = jwtService.extractRole(jwt);
                Long orgId = jwtService.extractOrgId(jwt);

                UserPrincipal userPrincipal = null;
                if (userId != null && role != null && orgId != null) {
                    userPrincipal = new UserPrincipal(userId, userEmail, name, role, orgId);
                } else {
                    // Fallback to database lookup for backward compatibility and tests
                    Optional<User> userOpt = userRepository.findByEmail(userEmail);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        userPrincipal = new UserPrincipal(
                            user.getId(),
                            user.getEmail(),
                            user.getName(),
                            user.getRole().name(),
                            user.getOrganization() != null ? user.getOrganization().getId() : null
                        );
                    }
                }

                if (userPrincipal != null && jwtService.isTokenValid(jwt, userPrincipal.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Suppress exceptions here. Let Spring Security entry point catch unauthorized accesses
        }

        filterChain.doFilter(request, response);
    }
}
