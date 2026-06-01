package com.jobtracker.api.controller;

import com.jobtracker.api.dto.JobApplicationRequest;
import com.jobtracker.api.dto.JobApplicationResponse;
import com.jobtracker.api.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
@Tag(name = "Job Application", description = "Manage your job applications")
@RestController
@RequestMapping("/applications")
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @Operation(summary = "Get All job applications of the user")
    @GetMapping
    public List<JobApplicationResponse> getAllJobApplications() {
        return jobApplicationService.getAllJobApplications();
    }
    @Operation(summary = "Create new job applications")
    @ApiResponse(responseCode="201", description = "Job application created successfully")
    @ApiResponse(responseCode = "400",description = "Validation Failure")
    @PostMapping
    public ResponseEntity<Void> createJobApplication(@RequestBody @Valid JobApplicationRequest jobApplicationRequest) {
        JobApplicationResponse saved = jobApplicationService.createJobApplication(jobApplicationRequest);
        return ResponseEntity.created(URI.create("/applications/" + saved.id())).build();
    }

    @Operation(summary = "Get a job application by ID")
    @ApiResponse(responseCode = "200", description = "Application found")
    @ApiResponse(responseCode = "404", description = "Application not found")
    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getJobApplication(@PathVariable long id) {
        JobApplicationResponse jobApplication = jobApplicationService.getJobApplication(id);
        return ResponseEntity.ok(jobApplication);
    }
    @Operation(summary = "Update job application by ID")
    @ApiResponse(responseCode = "204", description = "Application updated")
    @ApiResponse(responseCode = "404", description = "Application not found")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateJobApplication(@PathVariable Long id, @RequestBody @Valid JobApplicationRequest jobApplicationRequest) {

        jobApplicationService.updateJobApplication(id, jobApplicationRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete job application by ID")
    @ApiResponse(responseCode = "204", description = "Application deleted")
    @ApiResponse(responseCode = "404", description = "Application not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable long id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.noContent().build();
    }
}
