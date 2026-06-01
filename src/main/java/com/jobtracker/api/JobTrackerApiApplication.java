package com.jobtracker.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobTrackerApiApplication.class, args);
    }

	/*@Bean
    CommandLineRunner testRepository(JobApplicationRepository repository) {
		return args -> {
			JobApplication jobApplication = new JobApplication();
			jobApplication.setCompanyName("Google");
			jobApplication.setJobTitle("Java Developer");
			jobApplication.setStatus(ApplicationStatus.APPLIED);
			jobApplication.setDateApplied(LocalDate.now());

			JobApplication saved = repository.save(jobApplication);
			System.out.println("Saved application with ID: " + saved.getId());

			System.out.println("Total Applications: "+ repository.count());
		};
	}*/
}
