package com.creditbook.controller;

import com.creditbook.dto.PasswordChangeRequest;
import com.creditbook.dto.RegisterRequest;
import com.creditbook.dto.UserProfileDto;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileDto profile = authService.getCurrentUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        UserProfileDto updated = authService.updateProfile(currentUser.getId(), request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {
        authService.changePassword(currentUser.getId(), request, httpRequest.getRemoteAddr());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}
