package com.jobtracker.user.repository;

import com.jobtracker.user.model.Role;
import com.jobtracker.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    private User user1;

    @BeforeEach
    void setUp() throws Exception {
        user1 = User.builder()
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        entityManager.persist(user1);
        entityManager.flush();
    }

    @Test
    void findByEmail_returnsUserWhenExists() {
        Optional<User> existingUser = userRepository.findByEmail("test@example.com");
        assertThat(existingUser).isPresent();
        assertThat(existingUser.get().getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    void findByEmail_returnsEmptyWhenUserDoesNotExist() {
        Optional<User> existingUser = userRepository.findByEmail("xyz@example.com");
        assertThat(existingUser).isEmpty();
    }
}