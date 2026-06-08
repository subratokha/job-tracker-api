package com.jobtracker.application.service;


import com.jobtracker.application.dto.JobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.exception.ResourceNotFoundException;
import com.jobtracker.application.model.JobApplication;
import com.jobtracker.application.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional
    public JobApplicationResponse createJobApplication(JobApplicationRequest request, Long userId) {
        JobApplication entity = mapRequestToEntity(request, null);
        entity.setUserId(userId);
        JobApplication saved = jobApplicationRepository.save(entity);
        return new JobApplicationResponse(saved);
    }


    public JobApplicationResponse getJobApplication(Long jobApplicationId, Long userId) {
        JobApplication jobApplication = getCurrentUserApplication(jobApplicationId, userId);
        return new JobApplicationResponse(jobApplication);
    }

    @Transactional
    public void updateJobApplication(Long jobApplicationId, JobApplicationRequest request, Long userId) {
        JobApplication existing = getCurrentUserApplication(jobApplicationId, userId);
        mapRequestToEntity(request, existing);
        jobApplicationRepository.save(existing);
    }

    private JobApplication getCurrentUserApplication(Long jobApplicationId, Long userId) {

        return jobApplicationRepository
                .findByIdAndUserId(jobApplicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application Not Found"));
    }

    @Transactional
    public void deleteJobApplication(Long jobApplicationId, Long userId) {
        JobApplication existing = getCurrentUserApplication(jobApplicationId, userId);
        jobApplicationRepository.delete(existing);
    }


    public List<JobApplicationResponse> getAllJobApplications(Long userId) {
        return jobApplicationRepository.findAllByUserId(userId)
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
