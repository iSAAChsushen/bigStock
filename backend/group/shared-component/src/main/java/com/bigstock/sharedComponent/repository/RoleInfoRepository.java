package com.bigstock.sharedComponent.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bigstock.sharedComponent.entity.RoleInfo;

public interface RoleInfoRepository extends JpaRepository<RoleInfo, BigInteger> {

    // Optional: Add custom finder methods here

    // Example:
	Optional<RoleInfo> findByRoleId(String roleId);
}