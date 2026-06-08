package com.jobtracker.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String jobTitle;
    private String jobUrl;
    private LocalDate dateApplied;
    private LocalDate lastFollowUpDate;
    private String contactName;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    @Column(length = 1000)
    private String notes;
    @Column(nullable = false)
    private Long userId;
}
