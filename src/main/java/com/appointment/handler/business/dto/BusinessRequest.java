package com.appointment.handler.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String name;

    private String description;

    @NotBlank(message = "Business type is required")
    private String businessType;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Business phone number is required")
    private String phone;
}
