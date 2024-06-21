package com.bigstock.sharedComponent.entity;

import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.ToString;

@Entity
@ToString
@Data
@Table(schema = "bstock", name = "user_account")
public class UserAccount {

    @Id
    @Column(name = "id")
    private BigInteger id;
    

    @Column(name = "user_account", unique = true, nullable = false)
    private String user_account;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mail", unique = true, nullable = false)
    private String email;

    @Column(name = "gender")
    private String gender;

    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "role_id")
    private String roleId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "user_password")
    private String userPassword;
    
    @Column(name = "status")
    private String status;
}
