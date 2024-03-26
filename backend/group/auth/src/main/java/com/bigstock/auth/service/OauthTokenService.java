package com.bigstock.auth.service;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bigstock.auth.domain.vo.UserInloginInfo;
import com.bigstock.sharedComponent.entity.RoleInfo;
import com.bigstock.sharedComponent.entity.UserAccount;
import com.bigstock.sharedComponent.service.RoleInfoService;
import com.bigstock.sharedComponent.service.UserAccountService;
import com.google.common.collect.Lists;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OauthTokenService {

	@Value("${server.oauth2.secret-key}")
	private String secretKey;

	@Value("${server.oauth2.key}")
	private String keyPath;

	private final RoleInfoService roleInfoService;

	private final UserAccountService userAccountService;

	private final RedissonClient redissonClient;

	private final BCryptPasswordEncoder passwordEncoder; // 假设使用 BCryptPasswordEncoder 加密密码

	public String userLoginHandle(UserInloginInfo userInloginInfo) {
		String username = userInloginInfo.getUserName();
		String password = userInloginInfo.getPassword();
		Optional<UserAccount> userAccountOp = Optional.empty();
		userAccountOp = userAccountService.findByEmailIgnoreCase(username).stream().findFirst();
		if (userAccountOp.isEmpty()) {
			userAccountOp = userAccountService.getByPhone(username).stream().findFirst();
		}
		UserAccount userAccount = userAccountOp.orElseThrow(() -> new JwtException("user can not found"));
		// 验证密码
		if (!passwordEncoder.matches(password, userAccount.getUserPassword())) {
			throw new JwtException("invalid password");
		}
		String accessToken = generateAccessToken(username, userAccount);
		String refreshToken = generateRefreshToken(username);
		// 將新的 refresh token 存入資料庫
		RBucket<Object> refreshTokenRb = redissonClient.getBucket("refresh_token:" + username);
		refreshTokenRb.set(refreshToken);
		refreshTokenRb.expire(Duration.ofHours(4));
		// 將新的access token倒回去Redis
		RBucket<Object> accessTokenRb = redissonClient.getBucket("access_token:" + username);
		accessTokenRb.set(accessToken);
		accessTokenRb.expire(Duration.ofHours(1));
		return accessToken;
	}

	public String refreshToken(String refreshToken) {

		// 獲取 refresh token 中的資料
		Claims claims = parseJwtToken(refreshToken);
		// 建立新的 access token 和 refresh token
		String newAccessToken = generateAccessToken(claims.getSubject(), claims.get("email", String.class),
				claims.get("phone", String.class));
		RBucket<Object> accessTokenRb = redissonClient.getBucket("access_token:" + claims.getSubject());
		accessTokenRb.set(newAccessToken);
		accessTokenRb.expire(Duration.ofHours(1));
//		ROLE_
		// 返回新的 access token 和 refresh token
		return new JSONObject().put("access_token", newAccessToken).toString();
	}

	private Claims parseJwtToken(String token) throws JwtException {
		Jws<Claims> jws = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
				.parseSignedClaims(token);
		return jws.getPayload();
	}

	public String generateAccessToken(String subject, UserAccount userAccount) {
		// 使用 Jwts.builder() 建立 JWT
		JwtBuilder builder = Jwts.builder();
		String roleId = userAccount.getRoleId();
		RoleInfo roleInfo = roleInfoService.getByRoleId(roleId)
				.orElseThrow(() -> new JwtException("role can not found"));
		// 設定 JWT 主體
		builder.subject(subject);
		builder.claim("roles", Lists.newArrayList("ROLE_"+roleInfo.getRoleName()));
		// 設定 JWT 發行時間
		builder.issuedAt(new Date());

		// 設定 JWT 有效期
		builder.expiration(new Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()));

		// 添加 header
		builder.header().add("typ", "JWT").and();
		builder.header().add("alg", "HS256").and();
		// 設定 JWT 簽名
		builder.signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), Jwts.SIG.HS256);
		// 建立並返回 JWT
		String token = builder.compact();

		// 返回 Bearer 令牌
		return "Bearer " + token;
	}

	public String generateAccessToken(String email, String phone, String subject) {
		// 使用 Jwts.builder() 建立 JWT
		JwtBuilder builder = Jwts.builder();
		Optional<UserAccount> userAccountsOp = Optional.empty();
		if (email != null && StringUtils.isNotBlank(email)) {
			userAccountsOp = userAccountService.findByEmailIgnoreCase(email).stream().findFirst();
		}
		if (phone != null && StringUtils.isNotBlank(phone)) {
			userAccountsOp = userAccountService.getByPhone(phone).stream().findFirst();
		}
		if (userAccountsOp.isEmpty()) {
			throw new JwtException("invalid token");
		}
		String roleId = userAccountsOp.get().getRoleId();
		RoleInfo roleInfo = roleInfoService.getByRoleId(roleId)
				.orElseThrow(() -> new JwtException("role can not found"));
		// 設定 JWT 主體
		builder.subject(subject);
		builder.claim("roles", Lists.newArrayList("ROLE_"+roleInfo.getRoleName()));
		// 設定 JWT 發行時間
		builder.issuedAt(new Date());

		// 設定 JWT 有效期
		builder.expiration(new Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()));
		// 添加 header
		builder.header().add("typ", "JWT").and();
		builder.header().add("alg", "HS256").and();
		// 設定 JWT 簽名
		builder.signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), Jwts.SIG.HS256);
		// 建立並返回 JWT
		return builder.compact();
	}

	private String generateRefreshToken(String subject) {
		// 使用 Jwts.builder() 建立 JWT
		JwtBuilder builder = Jwts.builder();

		// 設定 JWT 主體
		builder.subject(subject);

		// 設定 JWT 發行時間
		builder.issuedAt(new Date());

		// 設定 JWT 有效期
		builder.expiration(new Date(System.currentTimeMillis() + Duration.ofHours(4).toMillis()));

		// 添加 header
		builder.header().add("typ", "JWT").and();
		builder.header().add("alg", "HS256").and();
		// 設定 JWT 簽名
		builder.signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), Jwts.SIG.HS256);

		// 建立並返回 JWT
		return builder.compact();
	}
}