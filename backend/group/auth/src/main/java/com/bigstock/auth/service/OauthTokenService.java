package com.bigstock.auth.service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.UserAccount;
import com.bigstock.sharedComponent.service.RoleInfoService;
import com.bigstock.sharedComponent.service.UserAccountService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OauthTokenService {

	@Value("${server.oauth2.secret-key}")
	private String secretKey;

	private final RoleInfoService roleInfoService;

	private final UserAccountService userAccountService;

	public String refreshToken(String refreshToken) {

		// 獲取 refresh token 中的資料
		Claims claims = parseJwtToken(refreshToken);
		JSONObject subjectJs = new JSONObject(claims.getSubject());
		// 建立新的 access token 和 refresh token
		String newAccessToken = generateAccessToken(subjectJs);
		String newRefreshToken = generateRefreshToken(subjectJs);

		// 將新的 refresh token 存入資料庫
		saveRefreshToken(newRefreshToken, new JSONObject(claims.getSubject()));

		// 返回新的 access token 和 refresh token
		return new JSONObject().put("access_token", newAccessToken).put("refresh_token", newRefreshToken).toString();
	}

	private Claims parseJwtToken(String token) throws JwtException {
		Jws<Claims> jws = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
				.parseSignedClaims(token);
		return jws.getPayload();
	}

	public String generateAccessToken(JSONObject subjectJs) {
		// 使用 Jwts.builder() 建立 JWT
		JwtBuilder builder = Jwts.builder();
		Object emailOb = subjectJs.get("email");
		Object phoneOb = subjectJs.get("phone");
		Optional<UserAccount> userAccountsOp = Optional.empty();
		if (emailOb != null && StringUtils.isNotBlank(emailOb.toString())) {
			userAccountsOp = userAccountService.findByEmailIgnoreCase(emailOb.toString()).stream().findFirst();
		}
		if (phoneOb != null && StringUtils.isNotBlank(phoneOb.toString())) {
			userAccountsOp = userAccountService.getByPhone(phoneOb.toString()).stream().findFirst();
		}
		if (userAccountsOp.isEmpty()) {
			throw new JwtException("invalid token");
		}
		// 設定 JWT 主體
		builder.subject(subjectJs.toString());

		// 設定 JWT 發行時間
		builder.issuedAt(new Date());

		// 設定 JWT 有效期
		builder.expiration(new Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()));

		// 設定 JWT 簽名
		builder.signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), Jwts.SIG.HS512);
		// 建立並返回 JWT
		return builder.compact();
	}

	public String generateRefreshToken(String subject) {
		// 使用 Jwts.builder() 建立 JWT
		JwtBuilder builder = Jwts.builder();

		// 設定 JWT 主體
		builder.setSubject(subject);

		// 設定 JWT 發行時間
		builder.setIssuedAt(new Date());

		// 設定 JWT 有效期
		builder.setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME));

		// 設定 JWT 簽名
		builder.signWith(SignatureAlgorithm.HS512, SECRET);

		// 建立並返回 JWT
		return builder.compact();
	}
}
