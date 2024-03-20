package com.bigstock.auth.infra;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
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
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

	private final RedissonClient redissonClient;
	
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

	/**
	 * 判斷token的腳色
	 */
	public class JwtAuthenticationFilter extends OncePerRequestFilter {

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                    FilterChain filterChain) throws ServletException, IOException {

	        String token = request.getHeader("Authorization");

	        if (token != null && token.startsWith("Bearer ")) {
	            token = token.substring(7);

	            try {
	                // Parse the JWT token using jjwt
	            	Jws<Claims> jwsClaims = Jwts.parser().setSigningKey("your-secret-key").build().parseClaimsJws(token);
	                Claims claims = jwsClaims.getBody();
	                JwsHeader jwsHeader  = jwsClaims.getHeader();
	                RBucket<Long> tokenBucket = redissonClient.getBucket("TOKEN_" + token);

	                if (tokenBucket != null) {
	                    // Token 存在，就延長2小時
	                	tokenBucket.expire(Duration.ofHours(2));
	                } else {
	                    // Token 不存在就回錯
	                	throw new ExpiredJwtException(jwsHeader, claims, "JWT token expired");
	                }
	                // Extract roles from the claims (assuming roles are stored under a "roles" claim)
	                List<String> roles = claims.get("roles", List.class);

	                // Convert roles to SimpleGrantedAuthority objects
	                String username = claims.getSubject();
	                List<SimpleGrantedAuthority> authorities = roles.stream()
	                        .map(SimpleGrantedAuthority::new)
	                        .collect(Collectors.toList());

	                // Create Authentication object with extracted roles
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