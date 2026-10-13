package com.creditbook.service;

import com.creditbook.dto.*;
import com.creditbook.entity.User;
import com.creditbook.exception.BadRequestException;
import com.creditbook.exception.ResourceNotFoundException;
import com.creditbook.repository.UserRepository;
import com.creditbook.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("An account with this email address already exists");
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getBusinessName(),
                request.getPhone()
        );

        User savedUser = userRepository.save(user);

        auditLogService.recordLog(savedUser, "USER_REGISTER", "USER", savedUser.getId(),
                "Registered new user account: " + savedUser.getEmail(), ipAddress);

        String token = tokenProvider.generateTokenFromEmail(savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getBusinessName()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String email = request.getEmail().toLowerCase().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = tokenProvider.generateToken(authentication);

        auditLogService.recordLog(user, "USER_LOGIN", "USER", user.getId(),
                "Successful user authentication", ipAddress);

        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getBusinessName()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(Long userId) {
        User user = getUserById(userId);
        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getBusinessName(),
                user.getPhone(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, RegisterRequest request, String ipAddress) {
        User user = getUserById(userId);
        user.setFullName(request.getFullName().trim());
        user.setBusinessName(request.getBusinessName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        auditLogService.recordLog(user, "USER_PROFILE_UPDATE", "USER", user.getId(),
                "Updated user profile details", ipAddress);

        return getCurrentUserProfile(userId);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request, String ipAddress) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.recordLog(user, "PASSWORD_CHANGE", "USER", user.getId(),
                "Changed account password", ipAddress);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
