package com.vega.userservice.controller;

import com.vega.userservice.dto.AuthResponse;
import com.vega.userservice.dto.UserLoginRequest;
import com.vega.userservice.dto.UserRegistrationRequest;
import com.vega.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * Kullanıcı kayıt endpoint'i. POST /api/auth/register
     * UserService.register() metodunu çağırır ve AuthResponse döner.
     * Giriş: UserRegistrationRequest (username, email, password, firstName, lastName)
     * Çıktı: 200 OK ile AuthResponse (token ve kullanıcı bilgileri)
     * 
     * @param request Kayıt isteği
     * @return ResponseEntity<AuthResponse> (200 OK)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kullanıcı login endpoint'i. POST /api/auth/login
     * UserService.login() metodunu çağırır ve AuthResponse döner.
     * Giriş: UserLoginRequest (usernameOrEmail, password)
     * Çıktı: 200 OK ile AuthResponse (token ve kullanıcı bilgileri)
     * 
     * @param request Login isteği
     * @return ResponseEntity<AuthResponse> (200 OK)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Token validation endpoint'i. POST /api/auth/validate
     * Authorization header'ından token'ı alır, UserService.validateToken() metodunu çağırır.
     * Giriş: Authorization header (Bearer token veya sadece token)
     * Çıktı: 200 OK ile Boolean (token geçerliyse true, değilse false)
     * 
     * @param token Authorization header (Bearer prefix'i otomatik kaldırılır)
     * @return ResponseEntity<Boolean> (200 OK)
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean isValid = userService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
    
    /**
     * Token'dan user ID çıkarma endpoint'i. POST /api/auth/user-id
     * Authorization header'ından token'ı alır, UserService.getUserIdFromToken() metodunu çağırır.
     * Giriş: Authorization header (Bearer token veya sadece token)
     * Çıktı: 200 OK ile Long (user ID) veya 404 Not Found (token geçersizse veya kullanıcı bulunamazsa)
     * 
     * @param token Authorization header (Bearer prefix'i otomatik kaldırılır)
     * @return ResponseEntity<Long> (200 OK veya 404 Not Found)
     */
    @PostMapping("/user-id")
    public ResponseEntity<Long> getUserIdFromToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Long userId = userService.getUserIdFromToken(token);
            if (userId != null) {
                return ResponseEntity.ok(userId);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Token'dan username çıkarma endpoint'i. POST /api/auth/username
     * Authorization header'ından token'ı alır, UserService.getUsernameFromToken() metodunu çağırır.
     * Push ve Pull Service'ler tarafından repository ID oluşturmak için kullanılır (username/repository-name formatı).
     * Giriş: Authorization header (Bearer token veya sadece token)
     * Çıktı: 200 OK ile String (username) veya 404 Not Found (token geçersizse)
     * 
     * @param authHeader Authorization header (Bearer prefix'i otomatik kaldırılır)
     * @return ResponseEntity<String> (200 OK veya 404 Not Found)
     */
    /**
     * Combined user-info endpoint — returns userId + username in one call.
     * Push/Pull services call this instead of separate /user-id + /username + /validate.
     */
    @PostMapping("/user-info")
    public ResponseEntity<java.util.Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            if (!userService.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }
            Long userId = userService.getUserIdFromToken(token);
            String username = userService.getUsernameFromToken(token);
            if (userId == null || username == null || username.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(java.util.Map.of("userId", userId, "username", username, "valid", true));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/username")
    public ResponseEntity<String> getUsernameFromToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String username = userService.getUsernameFromToken(token);
            if (username != null && !username.isEmpty()) {
                return ResponseEntity.ok(username);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

