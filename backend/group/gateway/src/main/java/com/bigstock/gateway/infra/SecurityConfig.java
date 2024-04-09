package com.bigstock.gateway.infra;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.bigstock.gateway.domain.vo.TokenInfo;
import com.bigstock.gateway.protocol.OAuth2Client;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
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
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception { // disable CSRF
		http.addFilterBefore(new CustomWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION).cors()
				.configurationSource(corsConfiguration()).and()
				.authorizeExchange(exchanges -> exchanges.pathMatchers(HttpMethod.OPTIONS).permitAll()
						// EAM 系統
						.pathMatchers(HttpMethod.POST, "/gateway/**").access(new JwtReactiveAuthorizationManager())
						.pathMatchers(HttpMethod.POST, "/auth/**").permitAll()
						.pathMatchers(HttpMethod.POST, "/api/auth/**").access(new JwtReactiveAuthorizationManager())
						.pathMatchers(HttpMethod.GET, "/actuator/health").permitAll().anyExchange().authenticated())
				.csrf().disable().httpBasic().disable().formLogin().disable();

		return http.build();
	}

	CorsConfigurationSource corsConfiguration() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.applyPermitDefaultValues();
		corsConfig.addAllowedMethod(HttpMethod.GET);
		corsConfig.addAllowedMethod(HttpMethod.POST);
		corsConfig.addAllowedMethod(HttpMethod.PATCH);
		corsConfig.addAllowedMethod(HttpMethod.PUT);
		corsConfig.addAllowedMethod(HttpMethod.DELETE);
		corsConfig.addAllowedMethod(HttpMethod.OPTIONS);
//		corsConfig.setAllowedOrigins(Arrays.asList("http://127.0.0.1:8081", "http://localhost:8080"));
		corsConfig.setAllowedOrigins(Arrays.asList("*"));
		corsConfig.setAllowedHeaders(Arrays.asList("*"));
		corsConfig.setMaxAge(36000L);
		corsConfig.setAllowCredentials(false); // When allowCredentials is true, allowedOrigins cannot contain the
												// special value "*"

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}

	public class JwtReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

		@Override
		public Mono<AuthorizationDecision> check(Mono<Authentication> authenticationMono,
				AuthorizationContext authorizationContext) {
			return authenticationMono.flatMap(this::checkAuthorities).defaultIfEmpty(new AuthorizationDecision(true));
		}

		private Mono<AuthorizationDecision> checkAuthorities(Authentication auth) {
			return null;
		}
	}

	/**
	 * 判斷token的腳色
	 */
//	public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//		/**
//		 * 解析Access Token內容
//		 * 
//		 * @param token access Token
//		 * @return Claims token的聲明資訊
//		 * @throws JwtException
//		 */
//		private Claims parseJwtToken(String token) throws JwtException {
//			Jws<Claims> jws = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
//					.parseSignedClaims(token);
//			return jws.getPayload();
//		}
//
//		/**
//		 * 使用refresh token 處理 token 刷新
//		 * 
//		 * @param claims
//		 * @return
//		 */
//		private boolean handleTokenRefresh(Claims claims, RBucket<Object> refreshToken) {
//			if (refreshToken.isExists()) {
//				Authentication newAuthentication = tryRefreshToken(refreshToken.get().toString());
//				if (newAuthentication != null) {
//					SecurityContextHolder.getContext().setAuthentication(newAuthentication);
//					refreshToken.expire(Duration.ofHours(4));
//					return true;
//				}
//			}
//			return false;
//		}
//
//		/**
//		 * 從token的聲明(Claims) 取出 事先放入到acces token的角色資訊(roles) 跟 userName(Subject)
//		 * ，並且把這些資訊放入到UsernamePasswordAuthenticationToken
//		 * 讓Controller中有使用 @Secured @PreAuthorize 等註解可以生效
//		 * 
//		 * @param claims jwt 的聲明類
//		 * @return Authentication
//		 */
//		@SuppressWarnings("unchecked")
//		private Authentication createAuthentication(Claims claims) {
//			List<String> roles = (List<String>) claims.get("roles", List.class);
//			List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
//					.collect(Collectors.toList());
//			return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
//		}
//
//		private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
//			if (e instanceof MalformedJwtException) {
//				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT token");
//			} else if (e instanceof ExpiredJwtException) {
//				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
//			} else {
//				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//			}
//		}
//
//		@Override
//		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//				FilterChain filterChain) throws ServletException, IOException {
//
//			try {
//
//				String token = request.getHeader("Authorization");
//				if (request.getRequestURI().startsWith("/actuator/") || request.getRequestURI().startsWith("/gateway/")
//						|| request.getRequestURI().startsWith("/auth/")) {
//					filterChain.doFilter(request, response);
//					return;
//				}
//				if (token != null && token.startsWith("Bearer ")) {
//					token = token.substring(7);
//
//					Claims claims = parseJwtToken(token);
//					RBucket<Object> refreshToken = redissonClient.getBucket("refresh_token:" + claims.getSubject());
//					RBucket<Object> accessTokenRB = redissonClient.getBucket("access_token:" + claims.getSubject());
//
//					// 假設accessTokenRB不存在或client 帶的token與Redis的token不依樣的時候，依樣導回登入頁
//					if ((!accessTokenRB.isExists() || !token.equals(accessTokenRB.get().toString().split(" ")[1]))
//							&& !refreshToken.isExists()) {
//						response.sendRedirect("/login");
//					}
//					// 先檢查 access token 是否有效
//					if (accessTokenRB.isExists()) {
//						refreshToken.expire(Duration.ofHours(4));
//						accessTokenRB.expire(Duration.ofHours(1));
//						Authentication authentication = createAuthentication(claims);
//						SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
//						filterChain.doFilter(request, response);
//						return;
//					}
//
//					// 如果 access token 無效，再進行 refresh token 的流程
//					if (handleTokenRefresh(claims, refreshToken)) {
//						filterChain.doFilter(request, response);
//						return;
//					}
//					// 如果 access token 和 refresh token 都無效，則導回登入頁
//				}
//				return;
//			} catch (JwtException e) {
//				handleJwtException(response, e);
//			}
//		}
//
//		/**
//		 * 利用refreshToken 重新產生access token
//		 * 
//		 * @param refreshToken
//		 * @return
//		 */
//		private Authentication tryRefreshToken(String refreshToken) {
//
//			// Create an OAuth2Client instance
//			OAuth2Client oAuth2Client = Feign.builder().contract(new SpringMvcContract()).client(new OkHttpClient())
//					.encoder(new JacksonEncoder()).target(OAuth2Client.class, oauth2Url);
//			TokenInfo refreshTokenInfo = new TokenInfo();
//			refreshTokenInfo.setRefreshToken(refreshToken);
//			// Call the refresh token API
//			String newAccessToken = oAuth2Client.refreshToken(refreshTokenInfo);
//
//			// Store the new refresh token in Redis using redissonClient
//			return createAuthentication(parseJwtToken(newAccessToken));
//		}
//	}
	class CustomWebFilter implements WebFilter {

		private static final Logger log = LoggerFactory.getLogger(CustomWebFilter.class);

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			// 在这里实现您的过滤逻辑
			// 例如，记录每个请求的信息
			log.info("Request URL: {}", exchange.getRequest().getURI().toString());

			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			try {
				if (request.getPath().value().startsWith("/actuator/") || request.getPath().value().startsWith("/gateway/")
						|| request.getPath().value().startsWith("/auth/")) {
					return chain.filter(exchange);
				}
				String token = request.getHeaders().getFirst("Authorization");
				if (token == null || !token.startsWith("Bearer ")) {
					return unauthorized(response);
				}

				token = token.substring(7);

				Claims claims = parseJwtToken(token);
				RBucket<Object> refreshToken = redissonClient.getBucket("refresh_token:" + claims.getSubject());
				RBucket<Object> accessTokenRB = redissonClient.getBucket("access_token:" + claims.getSubject());
				// 假設accessTokenRB不存在或client带的token与Redis的token不一样的时候，依样导回登入页
				if (!request.getPath().value().startsWith("/actuator/")
						&& !request.getPath().value().startsWith("/gateway/")
						&& !request.getPath().value().startsWith("/auth/")) {
					if ((!accessTokenRB.isExists() || !token.equals(accessTokenRB.get().toString().split(" ")[1]))
							&& !refreshToken.isExists()) {
						return unauthorized(response);
					}
				}

				// 先检查 access token 是否有效
				if (accessTokenRB.isExists()) {
					refreshToken.expire(Duration.ofHours(4));
					accessTokenRB.expire(Duration.ofHours(1));
					Authentication authentication = createAuthentication(claims);
					SecurityContextHolder.getContext().setAuthentication(authentication);
					return chain.filter(exchange);
				}

				// 如果 access token 无效，再进行 refresh token 的流程
				if (handleTokenRefresh(claims, refreshToken)) {
					return chain.filter(exchange);
				}

				// 如果 access token 和 refresh token 都无效，則导回登入页
				return unauthorized(response);
			} catch (JwtException e) {
				return handleJwtException(response, e);
			}
		}

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
		private Mono<Void> handleJwtException(ServerHttpResponse response, JwtException e) {
			// 处理 JWT 异常的逻辑
			// 这里使用了假设的方法 handleJwtException，你需要实现它来处理 JWT 异常
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
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

		private void handleJwtException(HttpServletResponse response, JwtException e)
				throws IOException, java.io.IOException {
			if (e instanceof MalformedJwtException) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT token");
			} else if (e instanceof ExpiredJwtException) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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
					.encoder(new JacksonEncoder()).target(OAuth2Client.class, oauth2Url);
			TokenInfo refreshTokenInfo = new TokenInfo();
			refreshTokenInfo.setRefreshToken(refreshToken);
			// Call the refresh token API
			String newAccessToken = oAuth2Client.refreshToken(refreshTokenInfo);

			// Store the new refresh token in Redis using redissonClient
			return createAuthentication(parseJwtToken(newAccessToken));
		}

		private Mono<Void> unauthorized(ServerHttpResponse response) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}
	}

}
