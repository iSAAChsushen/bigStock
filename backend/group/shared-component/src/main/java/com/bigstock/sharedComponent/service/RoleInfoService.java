package com.bigstock.sharedComponent.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bigstock.sharedComponent.entity.RoleInfo;
import com.bigstock.sharedComponent.repository.RoleInfoRepository;

@Service
public class RoleInfoService {

	private final RoleInfoRepository roleInfoRepository;

	public RoleInfoService(RoleInfoRepository roleInfoRepository) {
		this.roleInfoRepository = roleInfoRepository;
	}

	public List<RoleInfo> getAll() {
		return roleInfoRepository.findAll();
	}

	public Optional<RoleInfo> getById(BigInteger id) {
		return roleInfoRepository.findById(id);
	}

	public RoleInfo insert(RoleInfo roleInfo) {
		return roleInfoRepository.save(roleInfo);
	}

	public List<RoleInfo> insert(List<RoleInfo> roleInfos) {
		return roleInfoRepository.saveAll(roleInfos);
	}

	public void deleteById(BigInteger id) {
		roleInfoRepository.deleteById(id);
	}

	public void delete(RoleInfo roleInfo) {
		roleInfoRepository.delete(roleInfo);
	}

	// Add additional methods for RedissonClient interactions as needed

	public Optional<RoleInfo> getByRoleId(String roleId) {
		return roleInfoRepository.findByRoleId(roleId);
	}
}
