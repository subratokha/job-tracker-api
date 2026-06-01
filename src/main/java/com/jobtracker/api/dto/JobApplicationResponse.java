package com.jobtracker.api.dto;

import com.jobtracker.api.model.ApplicationStatus;
import com.jobtracker.api.model.JobApplication;

import java.time.LocalDate;

public record JobApplicationResponse(
        Long id,
        String companyName,
        String jobTitle,
        String jobUrl,
        String contactName,
        LocalDate dateApplied,
        LocalDate lastFollowUpDate,
        ApplicationStatus status,
        String notes
) {
    // Convenience constructor — converts entity to DTO
    public JobApplicationResponse(JobApplication entity) {
        this(
                entity.getId(),
                entity.getCompanyName(),
                entity.getJobTitle(),
                entity.getJobUrl(),
                entity.getContactName(),
                entity.getDateApplied(),
                entity.getLastFollowUpDate(),
                entity.getStatus(),
                entity.getNotes()
        );
    }
}