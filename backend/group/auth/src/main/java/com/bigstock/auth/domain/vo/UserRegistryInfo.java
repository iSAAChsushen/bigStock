package com.bigstock.auth.domain.vo;

import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Account Value Object")
public class UserRegistryInfo {

    @Schema(description = "Phone number of the user", example = "12345678901")
    private String phone;

    @Schema(description = "Email address of the user", required = true, example = "example@mail.com")
    @NotBlank(message = "Mail is mandatory")
    private String mail;

    @Schema(description = "Gender of the user", example = "M")
    private String gender;

    @Schema(description = "Birth date of the user", example = "1990-01-01")
    private Date birthDate;

    @Schema(description = "Role ID of the user", example = "A")
    private String roleId;

    @Schema(description = "Time when the user account was created")
    private LocalDateTime createTime;

    @Schema(description = "Time when the user account was last updated")
    private LocalDateTime updateTime;

    @Schema(description = "Updated by user", example = "admin")
    private String updateBy;

    @Schema(description = "Password of the user account", example = "encryptedPassword")
    private String userPassword;

    @JsonIgnore
    @Builder.Default
    private String status = "2";

    @Schema(description = "Name of the user", example = "John Doe")
    private String userName;

    @Schema(description = "User account name", required = true, example = "john_doe")
    @NotBlank(message = "User account is mandatory")
    private String userAccount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setStatus(String status) {
        this.status = status;
    }
}
