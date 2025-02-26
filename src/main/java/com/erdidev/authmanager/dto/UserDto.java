package com.erdidev.authmanager.dto;

import com.erdidev.taskmanager.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "User Data Transfer Object")
public class UserDto extends BaseDto {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String fullName;
    private Set<String> roles;
    private boolean enabled;
} 