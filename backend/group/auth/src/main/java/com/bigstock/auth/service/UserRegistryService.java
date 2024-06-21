package com.bigstock.auth.service;

import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.service.UserAccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRegistryService {

	public UserAccountService userAccountService;

	public void registryUser() {

	}
}
