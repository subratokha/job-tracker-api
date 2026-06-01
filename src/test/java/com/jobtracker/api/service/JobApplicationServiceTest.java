package com.jobtracker.api.service;

import com.jobtracker.api.dto.JobApplicationRequest;
import com.jobtracker.api.dto.JobApplicationResponse;
import com.jobtracker.api.exception.ResourceNotFoundException;
import com.jobtracker.api.model.ApplicationStatus;
import com.jobtracker.api.model.JobApplication;
import com.jobtracker.api.model.User;
import com.jobtracker.api.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private JobApplicationService jobApplicationService;

    // shared test data
    private JobApplicationRequest request;
    private User user;
    private JobApplication jobApplication;
    private JobApplicationResponse jobApplicationResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        request = new JobApplicationRequest(
                "ING",
                "Java Developer",
                "",
                "test again",
                LocalDate.now(),
                null,
                ApplicationStatus.APPLIED,
                "Applied via LinkedIn"
        );
        jobApplication = new JobApplication();
        jobApplication.setId(1L);
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
    void createJobApplication_shouldAssignCurrentUser() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.save(ArgumentMatchers.any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        JobApplicationResponse jobApplicationResponse = jobApplicationService.createJobApplication(request);
        ArgumentCaptor<JobApplication> jobApplicationCaptor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(jobApplicationCaptor.capture());
        JobApplication savedEntity = jobApplicationCaptor.getValue();
        assertThat(savedEntity.getUser()).isEqualTo(user);
        assertThat(savedEntity.getCompanyName()).isEqualTo("ING");
        assertThat(savedEntity.getJobTitle()).isEqualTo("Java Developer");

        assertThat(jobApplicationResponse.id()).isEqualTo(100L);
        assertThat(jobApplicationResponse.companyName()).isEqualTo("ING");
    }

    @Test
    void getJobApplication_shouldReturnApplicationWhenFoundForCurrentUser() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(jobApplication));
        assertThat(jobApplicationService.getJobApplication(100L)).isEqualTo(jobApplicationResponse);
    }

    @Test
    void getJobApplication_shouldThrowWhenNotFoundForCurrentUser() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty()); // ← simulate not found

        assertThatThrownBy(() -> jobApplicationService.getJobApplication(100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllJobApplications_shouldReturnOnlyCurrentUserApplications() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findAllByUserId(anyLong())).thenReturn(List.of(jobApplication));
        assertThat(jobApplicationService.getAllJobApplications()).isEqualTo(List.of(jobApplicationResponse));
    }

    @Test
    void getAllJobApplications_shouldReturnEmptyListWhenUserHasNoApplications() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findAllByUserId(anyLong())).thenReturn(emptyList());
        assertThat(jobApplicationService.getAllJobApplications()).isEmpty();
    }

    @Test
    void updateJobApplication_shouldUpdateCurrentUserApplicationWhenFound() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(jobApplication));

        jobApplicationService.updateJobApplication(1L, request);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());

        JobApplication updatedEntity = captor.getValue();
        assertThat(updatedEntity.getCompanyName()).isEqualTo(request.companyName());
        assertThat(updatedEntity.getJobTitle()).isEqualTo(request.jobTitle());
        assertThat(updatedEntity.getStatus()).isEqualTo(request.status());
    }

    @Test
    void updateJobApplication_shouldThrowWhenNotFound() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.updateJobApplication(1L, request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteJobApplication_shouldDeleteCurrentUserApplicationWhenFound() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(jobApplication));
        jobApplicationService.deleteJobApplication(1L);
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).delete(captor.capture());
        JobApplication deletedEntity = captor.getValue();
        assertThat(deletedEntity).isEqualTo(jobApplication);
    }

    @Test
    void deleteJobApplication_shouldThrowWhenNotFound() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> jobApplicationService.deleteJobApplication(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

}