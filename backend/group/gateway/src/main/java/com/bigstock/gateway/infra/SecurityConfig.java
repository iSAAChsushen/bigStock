package com.bigstock.gateway.infra;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bigstock.gateway.protocol.OAuth2Client;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureAlgorithm;
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
		http.authorizeRequests(authorizeRequests -> authorizeRequests.requestMatchers("/public/**").permitAll()
				.requestMatchers("/private/**").authenticated())
				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.addFilterBefore(new JwtAuthenticationFilter(), BearerTokenAuthenticationFilter.class).csrf().disable();

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
					Jws<Claims> jwsClaims = Jwts.parser().setSigningKey("your-secret-key").build()
							.parseClaimsJws(token);
					Claims claims = jwsClaims.getBody();
					JwsHeader jwsHeader = jwsClaims.getHeader();
					RBucket<Long> tokenBucket = redissonClient.getBucket("TOKEN_" + token);

					if (tokenBucket != null) {
						// Token 存在，就延長2小時
						tokenBucket.expire(Duration.ofHours(2));
					} else {
						// Token 不存在就回錯
//	                	throw new ExpiredJwtException(jwsHeader, claims, "JWT token expired");
						// If the token is expired, check if the refresh token is present in the request
						String refreshToken = request.getHeader("Refresh-Token");
						if (refreshToken != null) {
							// Try to refresh the token
							Authentication newAuthentication = tryRefreshToken(refreshToken);

							if (newAuthentication != null) {
								// Set the new authentication in the security context
								SecurityContextHolder.getContext().setAuthentication(newAuthentication);

								// Generate a new JWT token
								String newToken = Jwts.builder().setSubject(newAuthentication.getName())
										.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
										.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();

								// Add the new token to the response header
								response.setHeader("Authorization", "Bearer " + newToken);

								// Continue the filter chain
								filterChain.doFilter(request, response);
								return;
							}
						}
					}
					// Extract roles from the claims (assuming roles are stored under a "roles"
					// claim)
					List<String> roles = claims.get("roles", List.class);

					// Convert roles to SimpleGrantedAuthority objects
					String username = claims.getSubject();
					List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
							.collect(Collectors.toList());

					// Create Authentication object with extracted roles
					Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
							authorities);

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

		private Authentication tryRefreshToken(String refreshToken) {

	        // Create an OAuth2Client instance
			OAuth2Client oAuth2Client =	Feign.builder()
	        .contract(new SpringMvcContract())
	        .client(new OkHttpClient())
	        .encoder(new JacksonEncoder())
	        .decoder(new JacksonDecoder())
	        .target(OAuth2Client.class, "");

	        // Call the refresh token API
			// Call the refresh token API
			String responseString = oAuth2Client.refreshToken(refreshToken);

			// Parse the response as a JSON object
			JSONObject response = new JSONObject(responseString);

			// Get the new access token and refresh token
			String newAccessToken = response.getString("access_token");
			String newRefreshToken = response.getString("refresh_token");
			Jws<Claims> jwsClaims = Jwts.parser().setSigningKey("your-secret-key").build()
					.parseClaimsJws(newAccessToken);
			Claims claims = jwsClaims.getBody();
			JwsHeader jwsHeader = jwsClaims.getHeader();
	        // Create a new Authentication object with the new access token
			List<String> roles = claims.get("roles", List.class);

			// Convert roles to SimpleGrantedAuthority objects
			String username = claims.getSubject();
			List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());

			// Create Authentication object with extracted roles
			Authentication newAuthentication = new UsernamePasswordAuthenticationToken(username, null,
					authorities);
			// 創建新的 Authentication 物件
	        return newAuthentication;
	    }

	}
}

