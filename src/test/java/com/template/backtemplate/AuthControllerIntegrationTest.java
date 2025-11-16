package com.template.backtemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.template.backtemplate.auth.AuthResultDto;
import com.template.backtemplate.auth.JwtUtils;
import com.template.backtemplate.forms.LoginForm;
import com.template.backtemplate.model.User;
import com.template.backtemplate.repository.UserRepository;
import com.template.backtemplate.repository.UserSessionRepository;
import com.template.backtemplate.repository.ValidationCodeRepository;
import com.template.backtemplate.service.AuthService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {
	private static final String ACTIVE_USER_EMAIL = "test@gmail.com";
	private static final String ACTIVE_USER_USERNAME = "test";
	private static final String ACTIVE_USER_PASSWORD = "12test34";

	private static final String INACTIVE_USER_EMAIL = "test2@gmail.com";
	private static final String INACTIVE_USER_USERNAME = "test2";
	private static final String INACTIVE_USER_PASSWORD = "test1234";

	private static final String NEW_USER_EMAIL = "test3@gmail.com";
	private static final String NEW_USER_USERNAME = "test3";
	private static final String NEW_USER_PASSWORD = "1234test";

	private static final String OTHER_USER_EMAIL = "test4@gmail.com";
	private static final String OTHER_USER_USERNAME = "test4";
	private static final String OTHER_USER_PASSWORD = "1234test1234";

    @Autowired
    private TestRestTemplate restTemplate;
@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthService authService;


	@BeforeEach
	void initialize() throws Exception {

		User user1 = authService.registerUser(ACTIVE_USER_EMAIL, ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD);
		authService.registerUser(INACTIVE_USER_EMAIL, INACTIVE_USER_USERNAME, INACTIVE_USER_PASSWORD);

		user1.setValidated(true);
		userRepository.save(user1);

	}

	@AfterEach
	void clean() {
		userRepository.deleteAll();
	}
    @Test
    public void loginSuccessful_setsRefreshTokenCookie() {
        // Prepare login request
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("test");
        loginForm.setPassword("12test34");

        // Send login request
        ResponseEntity<AuthResultDto> response = restTemplate.postForEntity(
            "/api/public/login", loginForm, AuthResultDto.class);

        // Assert status
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Assert body contains expected data
        AuthResultDto result = response.getBody();
        assertNotNull(result);
        // ...additional assertions...

        response.getHeaders().get(HttpHeaders.SET_COOKIE).forEach(cookie -> {
            if (cookie.startsWith("refreshToken=")) {
                assertTrue(cookie.contains("HttpOnly"));
            }
        });
        // Assert refresh token cookie is present

    }
}