package com.bigstock.sharedComponent.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bigstock.sharedComponent.entity.RoleInfo;

public interface RoleInfoRepository extends JpaRepository<RoleInfo, BigInteger> {

    // Optional: Add custom finder methods here

    // Example:
    RoleInfo findByRoleId(String roleId);
}