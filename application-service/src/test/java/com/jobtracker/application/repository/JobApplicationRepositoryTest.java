package com.jobtracker.application.repository;

import com.jobtracker.application.model.ApplicationStatus;
import com.jobtracker.application.model.JobApplication;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private EntityManager entityManager;

    private JobApplication jobApplication1;
    private JobApplication jobApplication2;
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final Long USER_ID_3 = 3L;

    @BeforeEach
    void setUp() {
        jobApplication1 = new JobApplication();
        jobApplication1.setUserId(USER_ID_1);
        jobApplication1.setJobTitle("Java Developer");
        jobApplication1.setJobUrl("https://example.com/java");
        jobApplication1.setStatus(ApplicationStatus.APPLIED);
        jobApplication1.setContactName("John Doe");
        jobApplication1.setLastFollowUpDate(LocalDate.now());
        jobApplication1.setCompanyName("TechCorp");
        jobApplication1.setDateApplied(LocalDate.now());
        entityManager.persist(jobApplication1);

        jobApplication2 = new JobApplication();
        jobApplication2.setUserId(USER_ID_2);
        jobApplication2.setJobTitle("Senior Developer");
        jobApplication2.setJobUrl("https://example.com/senior");
        jobApplication2.setStatus(ApplicationStatus.APPLIED);
        jobApplication2.setContactName("Jane Smith");
        jobApplication2.setLastFollowUpDate(LocalDate.now());
        jobApplication2.setCompanyName("DevSolutions");
        jobApplication2.setDateApplied(LocalDate.now());
        entityManager.persist(jobApplication2);
        entityManager.flush();
    }

    @Test
    void findAllByUserId_returnsListWhenUserHasApplications() {
        List<JobApplication> applications = jobApplicationRepository.findAllByUserId(USER_ID_1);

        assertThat(applications).hasSize(1);
        assertThat(applications.getFirst().getUserId()).isEqualTo(USER_ID_1);
        assertThat(applications.getFirst().getJobTitle()).isEqualTo("Java Developer");
    }

    @Test
    void findAllByUserId_returnsEmptyWhenUserHasNoApplications() {
        List<JobApplication> applications = jobApplicationRepository.findAllByUserId(USER_ID_3);
        assertThat(applications).isEmpty();
    }

    @Test
    void findByIdAndUserId_returnsApplicationWhenFound() {
        Optional<JobApplication> application = jobApplicationRepository.findByIdAndUserId(jobApplication1.getId(), USER_ID_1);
        assertThat(application).isPresent();
        assertThat(application.get().getJobTitle()).isEqualTo("Java Developer");
    }

    @Test
    void findByIdAndUserId_returnsEmptyWhenIdNotFound() {
        Optional<JobApplication> application = jobApplicationRepository.findByIdAndUserId(999L, USER_ID_1);
        assertThat(application).isEmpty();
    }

    @Test
    void findByIdAndUserId_returnsEmptyWhenApplicationBelongsToDifferentUser() {
        Optional<JobApplication> application = jobApplicationRepository.findByIdAndUserId(jobApplication1.getId(), USER_ID_2);
        assertThat(application).isEmpty();
    }
}
