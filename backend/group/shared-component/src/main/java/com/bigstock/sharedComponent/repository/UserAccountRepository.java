package com.bigstock.sharedComponent.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bigstock.sharedComponent.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, BigInteger> {

    List<UserAccount> findByPhone(String phone);

    // Find all users by email ignoring case
    List<UserAccount> findByEmailIgnoreCase(String email);

    // Find all users by birth date after a specific date
    List<UserAccount> findByBirthDateAfter(Date birthDate);

}

