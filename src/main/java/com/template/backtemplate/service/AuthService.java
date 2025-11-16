package com.template.backtemplate.service;

import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.template.backtemplate.auth.AuthResult;
import com.template.backtemplate.auth.AuthResultDto;
import com.template.backtemplate.auth.JwtResult;
import com.template.backtemplate.auth.JwtUtils;
import com.template.backtemplate.exceptions.EmailAlreadyInUseException;
import com.template.backtemplate.exceptions.EmptyFormFieldsException;
import com.template.backtemplate.exceptions.ExpiredValidationCodeException;
import com.template.backtemplate.exceptions.IncorrectValidationCodeException;
import com.template.backtemplate.exceptions.AlreadyUsedValidationCodeException;
import com.template.backtemplate.exceptions.InvalidCredentialsException;
import com.template.backtemplate.exceptions.InvalidJwtException;
import com.template.backtemplate.exceptions.NotValidatedAccountException;
import com.template.backtemplate.exceptions.SendEmailException;
import com.template.backtemplate.exceptions.TokenAlreadyUsedException;
import com.template.backtemplate.exceptions.UserAlreadyValidatedException;
import com.template.backtemplate.exceptions.UsernameAlreadyInUseException;
import com.template.backtemplate.model.UserSession;
import com.template.backtemplate.model.User;
import com.template.backtemplate.model.ValidationCode;
import com.template.backtemplate.model.dto.UserDto;
import com.template.backtemplate.repository.UserRepository;
import com.template.backtemplate.repository.UserSessionRepository;
import com.template.backtemplate.repository.ValidationCodeRepository;
import com.template.backtemplate.utils.ApiResponse;
import com.template.backtemplate.utils.ValidationCodeTypeEnum;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final ValidationCodeRepository validationCodeRepository;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final JwtUtils jwtUtils;

    private final UserSessionRepository sessionRepository;

    private final int AUTH_EXPIRATION_TIME = 60 * 10; // 10 min
    private final int REFRESH_EXPIRATION_TIME_REMEMBER_ME = 60 * 60 * 24 * 30;
    private final int REFRESH_EXPIRATION_TIME_NO_REMEMBER_ME = 60 * 30;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, ValidationCodeRepository validationCodeRepository,
            UserRepository userRepository, EmailService emailService, JwtUtils jwtUtils,
            UserSessionRepository sessionRepository) {
        this.authenticationManager = authenticationManager;
        this.validationCodeRepository = validationCodeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.sessionRepository = sessionRepository;
    }

    @Value("${mail.subject.account-activation}")
    private String accountActivationSubject;
    @Value("${mail.message.account-activation}")
    private String accountActivationMessage;
    @Value("${mail.subject.password-reset}")
    private String passwordResetSubject;
    @Value("${mail.message.password-reset}")
    private String passwordResetMessage;
    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${auth.cookie.domain}")
    private String cookieDomain;

    @Value("${auth.cookie.path}")
    private String cookiePath;

    @Value("${auth.cookie.secure}")
    private boolean cookieSecure;

    @Value("${auth.cookie.httpOnly}")
    private boolean cookieHttpOnly;

    @Value("${auth.cookie.sameSite}")
    private String cookieSameSite;

    public User registerUser(String email, String username, String password)
            throws UsernameAlreadyInUseException, EmailAlreadyInUseException, EmptyFormFieldsException,
            InstanceNotFoundException, SendEmailException, UserAlreadyValidatedException {
        if (userRepository.findByUsername(username) != null) {
            throw new UsernameAlreadyInUseException("An user is already using this username");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new EmailAlreadyInUseException("An user is already using this email");
        }
        if (username == null || password == null || email == null) {
            throw new EmptyFormFieldsException();
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password);
        User user = userRepository.save(new User(email, username, encodedPassword));
        createValidationCode(user.getUsername(), ValidationCodeTypeEnum.ACTIVATE_ACCOUNT);

        return user;
    }

    @Transactional
    public AuthResult authenticate(String username, String password, boolean rememberMe)
            throws InvalidCredentialsException, EmptyFormFieldsException, NotValidatedAccountException {
        if (username == null || password == null) {
            throw new EmptyFormFieldsException();
        }
        User user = userRepository.findByUsername(username);
        // We return invalid credentials and not user not found to avoid user
        // enumeration
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(username, password);
        try {
            authenticationManager.authenticate(authenticationRequest);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if (!user.isValidated()) {
            throw new NotValidatedAccountException("Account not validated");
        }
        UserSession newSession = sessionRepository.save(new UserSession(user));

        JwtResult authTokenResult = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getEmail(),
                AUTH_EXPIRATION_TIME, rememberMe,
                newSession.getId());
        JwtResult refreshTokenResult = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getEmail(),
                rememberMe ? REFRESH_EXPIRATION_TIME_REMEMBER_ME : REFRESH_EXPIRATION_TIME_NO_REMEMBER_ME, rememberMe,
                newSession.getId());

        return new AuthResult(authTokenResult.getJwt(), authTokenResult.getExpirationDate().getTime(),
                refreshTokenResult.getJwt(), newSession.getId(), new UserDto(user));
    }

    public ResponseEntity<ApiResponse<AuthResultDto>> processAuthResult(AuthResult authResult,
            HttpServletResponse response) {
        if (authResult == null) {
            return ResponseEntity.status(500).body(new ApiResponse<>(null));
        }
        Cookie cookie = new Cookie("REFRESH_TOKEN", authResult.getRefreshToken());
        cookie.setDomain(cookieDomain);
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
        return ResponseEntity.ok().body(new ApiResponse<>(
                new AuthResultDto(authResult.getAuthToken(), authResult.getSessionId(), authResult.getUser())));
    }

    public ValidationCode createValidationCode(String username, ValidationCodeTypeEnum type)
            throws InstanceNotFoundException, SendEmailException, EmptyFormFieldsException,
            UserAlreadyValidatedException {
        if (username == null) {
            throw new EmptyFormFieldsException();
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException();
        }
        if (type.equals(ValidationCodeTypeEnum.ACTIVATE_ACCOUNT)) {
            if (user.isValidated()) {
                throw new UserAlreadyValidatedException(username + " is already validated");
            }
        }
        ValidationCode validationCode = new ValidationCode(type);
        validationCode.setCreatedBy(user);
        validationCode.setUser(user);
        validationCodeRepository.save(validationCode);

        try {
            if (type.equals(ValidationCodeTypeEnum.ACTIVATE_ACCOUNT)) {
                emailService.sendSimpleMessage(user.getEmail(), accountActivationSubject,
                        accountActivationMessage + "\n\n"
                                + frontendUrl + "/validate/" + username + "/" + validationCode.getCode());
            } else if (type.equals(ValidationCodeTypeEnum.RESET_PASSWORD)) {
                emailService.sendSimpleMessage(user.getEmail(), passwordResetSubject,
                        passwordResetMessage + "\n\n"
                                + frontendUrl + "/reset-password/" + username + "/" + validationCode.getCode());
            }
        } catch (Exception e) {
            throw new SendEmailException("Error sending validation email");
        }
        return validationCode;
    }

    private void validateCode(String username, ValidationCodeTypeEnum type, String code)
            throws InstanceNotFoundException, ExpiredValidationCodeException, AlreadyUsedValidationCodeException,
            IncorrectValidationCodeException {

        List<ValidationCode> validationCodeList = validationCodeRepository
                .findByUserUsernameAndTypeOrderByCreatedAtDesc(username, type.getType());
        if (validationCodeList.isEmpty()) {
            throw new InstanceNotFoundException();
        }
        ValidationCode lastValidationCode = validationCodeList.get(0);
        if (lastValidationCode.getCode().equals(code) && lastValidationCode.isUsed()) {
            throw new AlreadyUsedValidationCodeException("Validation code was already used");
        }
        Calendar expirationDate = (Calendar) lastValidationCode.getCreatedAt().clone();
        expirationDate.add(Calendar.MINUTE, ValidationCode.EXPIRATION_MINUTES);
        if (lastValidationCode.getCode().equals(code) && !expirationDate.after(Calendar.getInstance())) {
            throw new ExpiredValidationCodeException("Validation code expired");
        }
        if (!lastValidationCode.getCode().equals(code)) {
            throw new IncorrectValidationCodeException("Incorrect validation code");
        }
        lastValidationCode.setUsed(true);
    }

    public void activateAccount(String username, String code)
            throws InstanceNotFoundException, ExpiredValidationCodeException,
            AlreadyUsedValidationCodeException, IncorrectValidationCodeException, EmptyFormFieldsException {
        if (username == null || code == null) {
            throw new EmptyFormFieldsException();
        }
        validateCode(username, ValidationCodeTypeEnum.ACTIVATE_ACCOUNT, code);
        User user = userRepository.findByUsername(username);
        user.setValidated(true);
        userRepository.save(user);
    }

    public void resetPassword(String username, String code, String newPassword)
            throws InstanceNotFoundException, ExpiredValidationCodeException,
            AlreadyUsedValidationCodeException, IncorrectValidationCodeException, EmptyFormFieldsException {
        if (username == null || code == null) {
            throw new EmptyFormFieldsException();
        }
        validateCode(username, ValidationCodeTypeEnum.RESET_PASSWORD, code);
        User user = userRepository.findByUsername(username);
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
    }

    public AuthResult refreshToken(String refreshToken) throws InvalidJwtException, TokenAlreadyUsedException {
        Authentication auth = jwtUtils.validateToken(refreshToken);
        User user = userRepository.findByUsername(auth.getName());
        Claims claims = jwtUtils.extractClaims(refreshToken);
        UUID tokenSessionId = UUID.fromString(claims.get("sessionId", String.class));
        UserSession tokenSession = sessionRepository.findById(tokenSessionId).orElse(null);
        Calendar tenSecondsAgo = Calendar.getInstance();
        tenSecondsAgo.add(Calendar.SECOND, -10);
        // If the session is null (not found) or the session is deleted more than 10
        // seconds ago,
        // we delete all the sessions of the user (danger of stolen token)
        if (tokenSession == null
                || tokenSession.getDeletedAt() != null && tokenSession.getDeletedAt().before(tenSecondsAgo)) {
            sessionRepository.deleteByUserId(user.getId());
            throw new TokenAlreadyUsedException("Refresh token already used");
        }
        // If the session has been deleted less than 10 seconds ago we keep the usual
        // flow
        // Best case: Its a normal refresh token flow
        // Worst case: This refresh token has been used twice in a row (race condition)
        // and we end up creating two new sessions. (Not a big deal)
        tokenSession.setDeletedAt(Calendar.getInstance());
        sessionRepository.save(tokenSession);

        // We remove all the sessions that have been created more than 30 days ago (to
        // reduce the size of the table)
        Calendar thirtyDaysAgo = Calendar.getInstance();
        thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);
        sessionRepository.deleteByUserIdAndCreatedAtBefore(user.getId(), thirtyDaysAgo);

        // We create a new session
        UserSession newSession = sessionRepository.save(new UserSession(user));
        // We create the new tokens, getting the rememberMe value from the old token
        boolean rememberMe = claims.get("rememberMe", Boolean.class);
        JwtResult authTokenResult = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getEmail(),
                AUTH_EXPIRATION_TIME, rememberMe,
                newSession.getId());
        JwtResult refreshTokenResult = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getEmail(),
                rememberMe ? REFRESH_EXPIRATION_TIME_REMEMBER_ME : REFRESH_EXPIRATION_TIME_NO_REMEMBER_ME, rememberMe,
                newSession.getId());
        return new AuthResult(authTokenResult.getJwt(), authTokenResult.getExpirationDate().getTime(),
                refreshTokenResult.getJwt(), newSession.getId(), new UserDto(user));
    }

}
