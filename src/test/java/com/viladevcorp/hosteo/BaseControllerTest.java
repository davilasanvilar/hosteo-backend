package com.viladevcorp.hosteo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.service.AuthService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseControllerTest {

    protected static final String ACTIVE_USER_EMAIL_1 = "test@gmail.com";
    protected static final String ACTIVE_USER_USERNAME_1 = "test";
    protected static final String ACTIVE_USER_PASSWORD_1 = "12test34";

    protected static final String ACTIVE_USER_EMAIL_2 = "test2@gmail.com";
    protected static final String ACTIVE_USER_USERNAME_2 = "test2";
    protected static final String ACTIVE_USER_PASSWORD_2 = "12test34";

    protected User user1;
    protected User user2;

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @BeforeAll
    protected void resetUsers() throws Exception {
        deleteAllUsers();
        User user1 = authService.registerUser(ACTIVE_USER_EMAIL_1, ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1);
        user1.setValidated(true);
        this.user1 = userRepository.save(user1);
        User user2 = authService.registerUser(ACTIVE_USER_EMAIL_2, ACTIVE_USER_USERNAME_2, ACTIVE_USER_PASSWORD_2);
        user2.setValidated(true);
        this.user2 = userRepository.save(user2);
    }

    protected void deleteAllUsers() {
        userRepository.deleteAll();
    }

}
