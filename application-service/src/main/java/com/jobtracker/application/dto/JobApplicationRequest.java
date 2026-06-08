package com.jobtracker.application.dto;


import com.jobtracker.application.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JobApplicationRequest(
        @NotBlank(message = "Company name is required")
        String companyName,

        @NotBlank(message = "Job title is required")
        String jobTitle,

        String jobUrl,

        String contactName,

        @NotNull(message = "Date applied is required")
        LocalDate dateApplied,

        LocalDate lastFollowUpDate,

        @NotNull(message = "Status is required")
        ApplicationStatus status,

        String notes
) {
}