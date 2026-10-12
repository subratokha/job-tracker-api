package com.jobtracker.application.service;

import com.jobtracker.application.dto.JobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.exception.ResourceNotFoundException;
import com.jobtracker.application.model.ApplicationStatus;
import com.jobtracker.application.model.JobApplication;
import com.jobtracker.application.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @InjectMocks
    private JobApplicationService jobApplicationService;

    private JobApplicationRequest request;
    private JobApplication jobApplication;
    private JobApplicationResponse jobApplicationResponse;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        request = new JobApplicationRequest(
                "ING",
                "Java Developer",
                "https://ing.com",
                "Jan de Vries",
                LocalDate.now(),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );
        jobApplication = new JobApplication();
        jobApplication.setId(1L);
        jobApplication.setUserId(userId);
        jobApplication.setJobTitle("Job Title");
        jobApplication.setJobUrl("Job URL");
        jobApplication.setStatus(ApplicationStatus.APPLIED);
        jobApplication.setContactName("Contact Name");
        jobApplication.setLastFollowUpDate(LocalDate.now());
        jobApplication.setCompanyName("xyz");
        jobApplication.setDateApplied(LocalDate.now());
        jobApplicationResponse = new JobApplicationResponse(jobApplication);
    }

    @Test
    void createJobApplication_shouldAssignUserId() {
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        JobApplicationResponse response = jobApplicationService.createJobApplication(request, userId);
        ArgumentCaptor<JobApplication> jobApplicationCaptor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(jobApplicationCaptor.capture());
        JobApplication savedEntity = jobApplicationCaptor.getValue();
        assertThat(savedEntity.getUserId()).isEqualTo(userId);
        assertThat(savedEntity.getCompanyName()).isEqualTo("ING");
        assertThat(savedEntity.getJobTitle()).isEqualTo("Java Developer");
        assertThat(response.id()).isEqualTo(100L);
    }

    @Test
    void getJobApplication_shouldReturnApplicationWhenFoundForUser() {
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(jobApplication));
        assertThat(jobApplicationService.getJobApplication(1L, userId)).isEqualTo(jobApplicationResponse);
    }

    @Test
    void getJobApplication_shouldThrowWhenNotFoundForUser() {
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> jobApplicationService.getJobApplication(100L, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllJobApplications_shouldReturnOnlyUserApplications() {
        when(jobApplicationRepository.findAllByUserId(anyLong())).thenReturn(List.of(jobApplication));
        assertThat(jobApplicationService.getAllJobApplications(userId)).isEqualTo(List.of(jobApplicationResponse));
    }

    @Test
    void getAllJobApplications_shouldReturnEmptyListWhenUserHasNoApplications() {
        when(jobApplicationRepository.findAllByUserId(anyLong())).thenReturn(emptyList());
        assertThat(jobApplicationService.getAllJobApplications(userId)).isEmpty();
    }

    @Test
    void updateJobApplication_shouldUpdateUserApplicationWhenFound() {
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(jobApplication));

        jobApplicationService.updateJobApplication(1L, request, userId);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());

        JobApplication updatedEntity = captor.getValue();
        assertThat(updatedEntity.getCompanyName()).isEqualTo(request.companyName());
        assertThat(updatedEntity.getJobTitle()).isEqualTo(request.jobTitle());
        assertThat(updatedEntity.getStatus()).isEqualTo(request.status());
    }

    @Test
    void updateJobApplication_shouldThrowWhenNotFound() {
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> jobApplicationService.updateJobApplication(1L, request, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteJobApplication_shouldDeleteUserApplicationWhenFound() {
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(jobApplication));
        jobApplicationService.deleteJobApplication(1L, userId);
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).delete(captor.capture());
        JobApplication deletedEntity = captor.getValue();
        assertThat(deletedEntity).isEqualTo(jobApplication);
    }

    @Test
    void deleteJobApplication_shouldThrowWhenNotFound() {
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> jobApplicationService.deleteJobApplication(1L, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
