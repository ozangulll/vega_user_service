package com.vega.userservice.controller;

import com.vega.userservice.dto.ChangePasswordRequest;
import com.vega.userservice.dto.UserProfileResponse;
import com.vega.userservice.dto.UserPublicDto;
import com.vega.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/search")
    public ResponseEntity<List<UserPublicDto>> searchUsers(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "limit", defaultValue = "30") int limit) {
        return ResponseEntity.ok(userService.searchUsers(q, limit));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserPublicDto> getPublicByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getPublicProfile(username));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserProfileResponse profile = userService.getUserProfile(username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            Authentication authentication,
            @RequestBody UserProfileResponse updateRequest) {
        try {
            String username = authentication.getName();
            UserProfileResponse updatedProfile = userService.updateUserProfile(username, updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        try {
            String username = authentication.getName();
            userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

