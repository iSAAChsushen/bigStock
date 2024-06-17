package com.bigstock.gateway.protocol;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bigstock.gateway.domain.vo.TokenInfo;
import com.bigstock.gateway.domain.vo.UserInloginInfo;

@FeignClient(name = "bigstock-auth")
public interface OAuth2Client {

    @PostMapping(value ="/auth/refreshToken", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String refreshToken(@RequestBody TokenInfo refreshToken);
    
    @PostMapping(value ="/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String authLogin(@RequestBody UserInloginInfo refreshToken);

}