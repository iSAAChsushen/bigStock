package com.bigstock.sharedComponent.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bigstock.sharedComponent.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    List<UserAccount> findByPhone(String phone);

    // Find all users by email ignoring case
    Optional<UserAccount> findByEmail(String email);

    // Find all users by birth date after a specific date
    List<UserAccount> findByBirthDateAfter(Date birthDate);

}

