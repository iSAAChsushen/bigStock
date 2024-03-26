package com.bigstock.gateway.infra;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

	@Value("${server.oauth2.secret-key}")
	private String secretKey;
	@Value("${server.oauth2.url}")
	private String oauth2Url;

	@Autowired
	private RedissonClient redissonClient;


	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
	    return authenticationConfiguration.getAuthenticationManager();
	}
	
	
	@Bean
	public JwtDecoder jwtDecoder() {
		SecretKey signingKey = new SecretKeySpec(secretKey.getBytes(), "HMAC-SHA-512");
		return NimbusJwtDecoder.withSecretKey(signingKey).build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		// 設置你的 JwtAuthenticationConverter 配置
		return jwtAuthenticationConverter;
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorize -> authorize
				// Allow login endpoint without authentication
				.requestMatchers("/login").permitAll()
				// Require authentication for all other requests
				.anyRequest().authenticated())
				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.addFilterAfter(new JwtAuthenticationFilter(), BearerTokenAuthenticationFilter.class)
				.csrf(AbstractHttpConfigurer::disable); // Explicitly disable CSRF

		return http.build();
	}

	/**
	 * 判斷token的腳色
	 */
	public class JwtAuthenticationFilter extends OncePerRequestFilter {

		private String extractTokenFromHeader(HttpServletRequest request) {
			String token = request.getHeader("Authorization");
			if (token != null && token.startsWith("Bearer ")) {
				return token.substring(7);
			}
			throw new JwtException("Token not exsist");
		}

		/**
		 * 解析Access Token內容
		 * 
		 * @param token access Token
		 * @return Claims token的聲明資訊
		 * @throws JwtException
		 */
		private Claims parseJwtToken(String token) throws JwtException {
			Jws<Claims> jws = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
					.parseSignedClaims(token);
			return jws.getPayload();
		}

		/**
		 * 使用refresh token 處理 token 刷新
		 * 
		 * @param claims
		 * @return
		 */
		private boolean handleTokenRefresh(Claims claims, RBucket<Object> refreshToken) {
			if (refreshToken.isExists()) {
				Authentication newAuthentication = tryRefreshToken(refreshToken.get().toString());
				if (newAuthentication != null) {
					SecurityContextHolder.getContext().setAuthentication(newAuthentication);
					refreshToken.expire(Duration.ofHours(4));
					return true;
				}
			}
			return false;
		}

		/**
		 * 從token的聲明(Claims) 取出 事先放入到acces token的角色資訊(roles) 跟 userName(Subject)
		 * ，並且把這些資訊放入到UsernamePasswordAuthenticationToken
		 * 讓Controller中有使用 @Secured @PreAuthorize 等註解可以生效
		 * 
		 * @param claims jwt 的聲明類
		 * @return Authentication
		 */
		@SuppressWarnings("unchecked")
		private Authentication createAuthentication(Claims claims) {
			List<String> roles = (List<String>) claims.get("roles", List.class);
			List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
			return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
		}

		private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
			if (e instanceof MalformedJwtException) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT token");
			} else if (e instanceof ExpiredJwtException) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
				FilterChain filterChain) throws ServletException, IOException {

			try {
				String token = extractTokenFromHeader(request);
				Claims claims = parseJwtToken(token);
				RBucket<Object> refreshToken = redissonClient.getBucket("refresh_token:" + claims.getSubject());
				RBucket<Object> accessTokenRB = redissonClient.getBucket("access_token:" + claims.getSubject());

				// 假設accessTokenRB不存在或client 帶的token與Redis的token不依樣的時候，依樣導回登入頁
				if (!accessTokenRB.isExists() || !token.equals(accessTokenRB.get().toString().split(" ")[1])) {
					response.sendRedirect("/login");
				}
				// 先檢查 access token 是否有效
				if (accessTokenRB.isExists()) {
					refreshToken.expire(Duration.ofHours(4));
					accessTokenRB.expire(Duration.ofHours(1));
					Authentication authentication = createAuthentication(claims);
					SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
					filterChain.doFilter(request, response);
					return;
				}

				// 如果 access token 無效，再進行 refresh token 的流程
				if (handleTokenRefresh(claims, refreshToken)) {
					filterChain.doFilter(request, response);
					return;
				}
				// 如果 access token 和 refresh token 都無效，則導回登入頁
				response.sendRedirect("/login");
			} catch (JwtException e) {
				handleJwtException(response, e);
			}
		}

		/**
		 * 利用refreshToken 重新產生access token
		 * 
		 * @param refreshToken
		 * @return
		 */
		private Authentication tryRefreshToken(String refreshToken) {

			// Create an OAuth2Client instance
			OAuth2Client oAuth2Client = Feign.builder().contract(new SpringMvcContract()).client(new OkHttpClient())
					.encoder(new JacksonEncoder()).decoder(new JacksonDecoder()).target(OAuth2Client.class, oauth2Url);

			// Call the refresh token API
			JSONObject response = new JSONObject(oAuth2Client.refreshToken(refreshToken));

			// Get the new access token and refresh token
			String newAccessToken = response.getString("access_token");

			// Store the new refresh token in Redis using redissonClient
			return createAuthentication(parseJwtToken(newAccessToken));
		}

	}
}
