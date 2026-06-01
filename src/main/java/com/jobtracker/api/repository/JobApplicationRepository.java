package com.jobtracker.api.repository;

import com.jobtracker.api.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findAllByUserId(Long userId);

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);
}
