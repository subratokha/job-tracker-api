package com.jobtracker.api.service;

import com.jobtracker.api.dto.JobApplicationRequest;
import com.jobtracker.api.dto.JobApplicationResponse;
import com.jobtracker.api.exception.ResourceNotFoundException;
import com.jobtracker.api.model.JobApplication;
import com.jobtracker.api.model.User;
import com.jobtracker.api.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final UserService userService;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository, UserService userService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.userService = userService;
    }

    @Transactional
    public JobApplicationResponse createJobApplication(JobApplicationRequest request) {
        User loggedinUser = userService.getCurrentAuthenticatedUser();
        JobApplication entity = mapRequestToEntity(request, null);
        entity.setUser(loggedinUser);
        JobApplication saved = jobApplicationRepository.save(entity);
        return new JobApplicationResponse(saved);
    }


    public JobApplicationResponse getJobApplication(Long jobApplicationId) {
        JobApplication jobApplication = getCurrentUserApplication(jobApplicationId);
        return new JobApplicationResponse(jobApplication);
    }

    @Transactional
    public void updateJobApplication(Long jobApplicationId, JobApplicationRequest request) {
        JobApplication existing = getCurrentUserApplication(jobApplicationId);
        mapRequestToEntity(request, existing);
        jobApplicationRepository.save(existing);
    }

    private JobApplication getCurrentUserApplication(Long jobApplicationId) {
        User loggedinUser = userService.getCurrentAuthenticatedUser();
        return jobApplicationRepository
                .findByIdAndUserId(jobApplicationId, loggedinUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job Application Not Found"));
    }

    @Transactional
    public void deleteJobApplication(Long jobApplicationId) {
        JobApplication existing = getCurrentUserApplication(jobApplicationId);
        jobApplicationRepository.delete(existing);
    }


    public List<JobApplicationResponse> getAllJobApplications() {
        User loggedinUser = userService.getCurrentAuthenticatedUser();
        return jobApplicationRepository.findAllByUserId(loggedinUser.getId())
                .stream()
                .map(JobApplicationResponse::new)
                .toList();
    }

    private JobApplication mapRequestToEntity(JobApplicationRequest request, JobApplication entity) {
        if (entity == null) {
            entity = new JobApplication();
        }
        entity.setCompanyName(request.companyName());
        entity.setJobTitle(request.jobTitle());
        entity.setJobUrl(request.jobUrl());
        entity.setContactName(request.contactName());
        entity.setDateApplied(request.dateApplied());
        entity.setLastFollowUpDate(request.lastFollowUpDate());
        entity.setStatus(request.status());
        entity.setNotes(request.notes());
        return entity;
    }
}
