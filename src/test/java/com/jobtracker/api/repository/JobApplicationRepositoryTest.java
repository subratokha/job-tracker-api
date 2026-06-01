package com.jobtracker.api.repository;

import com.jobtracker.api.model.ApplicationStatus;
import com.jobtracker.api.model.JobApplication;
import com.jobtracker.api.model.Role;
import com.jobtracker.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private User user1;
    private User user2;
    private User user3;
    private JobApplication jobApplication1;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        testEntityManager.persistAndFlush(user1);
        user2 = User.builder()
                .email("abc@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        testEntityManager.persistAndFlush(user2);
        user3 = User.builder()
                .email("xyz@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        testEntityManager.persistAndFlush(user3);

        jobApplication1 = new JobApplication();
        jobApplication1.setUser(user1);
        jobApplication1.setJobTitle("Job Title");
        jobApplication1.setJobUrl("Job URL");
        jobApplication1.setStatus(ApplicationStatus.APPLIED);
        jobApplication1.setContactName("Contact Name");
        jobApplication1.setLastFollowUpDate(LocalDate.now());
        jobApplication1.setCompanyName("xyz");
        jobApplication1.setDateApplied(LocalDate.now());

        testEntityManager.persistAndFlush(jobApplication1);
        JobApplication jobApplication2 = new JobApplication();
        jobApplication2.setUser(user2);
        jobApplication2.setJobTitle("Other User Job");
        jobApplication2.setJobUrl("Other URL");
        jobApplication2.setStatus(ApplicationStatus.APPLIED);
        jobApplication2.setContactName("Other Contact");
        jobApplication2.setLastFollowUpDate(LocalDate.now());
        jobApplication2.setCompanyName("abc");
        jobApplication2.setDateApplied(LocalDate.now());

        testEntityManager.persistAndFlush(jobApplication2);


    }

    @Test
    void findAllByUserId_returnsListWhenUserHasApplication() {
        List<JobApplication> jobApplicationList = jobApplicationRepository.findAllByUserId(user1.getId());

        assertThat(jobApplicationList).hasSize(1);
        assertThat(jobApplicationList.getFirst().getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    void findAllByUserId_returnsEmptyWhenUserHasNoApplications() {
        List<JobApplication> jobApplicationList = jobApplicationRepository.findAllByUserId(user3.getId());
        assertThat(jobApplicationList).isEmpty();
    }

    @Test
    void findByIdAndUserId_returnsApplicationWhenFound() {
        Optional<JobApplication> jobApplicationOptional = jobApplicationRepository.findByIdAndUserId(jobApplication1.getId(), user1.getId());
        assertThat(jobApplicationOptional).isPresent();
        assertThat(jobApplicationOptional.get().getJobTitle()).isEqualTo("Job Title");
    }

    @Test
    void findByIdAndUserId_returnsEmptyWhenIdNotFound() {
        Optional<JobApplication> jobApplicationOptional = jobApplicationRepository.findByIdAndUserId(100L, user1.getId());
        assertThat(jobApplicationOptional).isEmpty();

    }

    @Test
    void findByIdAndUserId_returnsEmptyWhenApplicationBelongsToDifferentUser() {
        Optional<JobApplication> jobApplicationOptional = jobApplicationRepository.findByIdAndUserId(jobApplication1.getId(), user2.getId());
        assertThat(jobApplicationOptional).isEmpty();
    }

}