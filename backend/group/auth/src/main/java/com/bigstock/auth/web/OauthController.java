package com.bigstock.auth.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bigstock.auth.service.OauthTokenService;

@RestController()
@RequestMapping(value = "oauth")
public class OauthController {
	@Autowired
    private OauthTokenService oauthTokenService;

    @PostMapping("/refresh_token")
    public String refreshToken(@RequestBody String refreshToken) {
        return oauthTokenService.refreshToken(refreshToken);
    }
}
