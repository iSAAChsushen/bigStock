package com.bigstock.sharedComponent.entity;

import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@ToString
@Data
@Table(schema = "bstock", name = "role_info")
public class RoleInfo {

	@Id
	@Column(name = "id")
	private BigInteger id;
	
	@Column(name = "role_id")
	private String roleId;
	
	@Column(name = "role_name")
	private String roleName;
	
	@Column(name = "create_time")
	private Date createTime;
	
	@Column(name = "update_time")
	private Date updateTime;
	
	@Column(name = "update_by")
	private String updateBy;
}
