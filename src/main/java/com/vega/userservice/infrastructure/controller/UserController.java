package com.vega.userservice.infrastructure.controller;

import com.vega.userservice.domain.dto.UserProfileResponse;
import com.vega.userservice.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
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
}
