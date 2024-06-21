package com.bigstock.auth.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bigstock.auth.domain.vo.TokenInfo;
import com.bigstock.auth.domain.vo.UserInloginInfo;
import com.bigstock.auth.service.OauthTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "auth")
@Tag(name = "Auth Controller", description = "Auth for loggin and api")
public class OauthController {
	@Autowired
    private OauthTokenService oauthTokenService;

	 @Operation(summary = "Get Example", description = "Get Example Description")
    @PostMapping(value="refreshToken", consumes = MediaType.APPLICATION_JSON_VALUE )
    public String refreshToken(@RequestBody TokenInfo refreshTokenInfo) {
        return oauthTokenService.refreshToken(refreshTokenInfo.getRefreshToken());
    }

	@Operation(summary = "Get Example", description = "Get Example Description")
	@PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String login(@RequestBody UserInloginInfo userInloginInfo) {
		return oauthTokenService.userLoginHandle(userInloginInfo);
	}
	
	
    
}
