package com.bigstock.auth.infra;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		// 設置你的 JwtAuthenticationConverter 配置
		return jwtAuthenticationConverter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http.authorizeRequests(authorizeRequests -> authorizeRequests
	            .requestMatchers("/public/**").permitAll()
	            .requestMatchers("/private/**").authenticated())
	        .oauth2ResourceServer(
	            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
	        .addFilterBefore(new JwtAuthenticationFilter(), BearerTokenAuthenticationFilter.class)
	        .csrf().disable();

	    return http.build();
	}

	public class JwtAuthenticationFilter extends OncePerRequestFilter {

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                    FilterChain filterChain) throws ServletException, IOException {

	        String token = request.getHeader("Authorization");

	        if (token != null && token.startsWith("Bearer ")) {
	            token = token.substring(7);

	            try {
	                // Parse the JWT token using jjwt
	                Claims claims = Jwts.parser().setSigningKey("your-secret-key").build().parseClaimsJws(token).getBody();

	                // Extract relevant information from claims (e.g., username, roles)
	                String username = claims.getSubject();
	                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")); // Adapt based on roles in claims

	                // Create Authentication object
	                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

	                SecurityContextHolder.getContext().setAuthentication(authentication);
	            } catch (MalformedJwtException e) {
	                log.error("Invalid JWT token: {}", e.getMessage());
	                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT token");
	                return;
	            } catch (ExpiredJwtException e) {
	            	log.error("Expired JWT token: {}", e.getMessage());
	                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
	                return;
	            } catch (Exception e) {
	            	log.error("An error occurred while parsing the JWT token.", e);
	                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	                return;
	            }
	        }

	        filterChain.doFilter(request, response);
	    }
	}

}