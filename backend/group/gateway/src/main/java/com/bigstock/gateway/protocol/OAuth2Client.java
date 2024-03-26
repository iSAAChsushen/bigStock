package com.bigstock.gateway.protocol;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bigstock.gateway.domain.vo.TokenInfo;

public interface OAuth2Client {

    @PostMapping(value ="/oauth/refreshToken", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String refreshToken(@RequestBody TokenInfo refreshToken);

}