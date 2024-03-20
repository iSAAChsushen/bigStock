package com.bigstock.gateway.protocol;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface OAuth2Client {

    @PostMapping("/api/refresh-token")
    public String refreshToken(@RequestParam("refresh_token") String refreshToken);

}