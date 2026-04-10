package com.vega.userservice.service;

import com.vega.userservice.dto.AuthResponse;
import com.vega.userservice.dto.UserLoginRequest;
import com.vega.userservice.dto.UserProfileResponse;
import com.vega.userservice.dto.UserPublicDto;
import com.vega.userservice.dto.UserRegistrationRequest;
import com.vega.userservice.model.User;
import com.vega.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * Kullanıcı adı veya email ile kullanıcıyı bulur ve Spring Security UserDetails objesi olarak döner.
     * UserRepository'den kullanıcıyı çeker, UserDetails builder ile Spring Security uyumlu obje oluşturur.
     * Giriş: username (kullanıcı adı veya email)
     * Çıktı: UserDetails objesi (username, password, authorities, account status bilgileri içerir)
     * 
     * @param username Kullanıcı adı veya email
     * @return UserDetails objesi
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRole().name())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
    
    /**
     * Yeni kullanıcı kaydeder. Username ve email'in unique olduğunu kontrol eder, şifreyi BCrypt ile hash'ler,
     * kullanıcıyı veritabanına kaydeder, JWT token oluşturur ve AuthResponse döner.
     * Giriş: request (username, email, password, firstName, lastName)
     * Çıktı: AuthResponse (token, userId, username, email, role, expiresIn)
     * 
     * @param request Kayıt isteği (username, email, password, firstName, lastName)
     * @return AuthResponse (token ve kullanıcı bilgileri)
     * @throws RuntimeException Username veya email zaten kullanılıyorsa fırlatılır
     */
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        UserDetails userDetails = loadUserByUsername(savedUser.getUsername());
        String token = jwtService.generateToken(userDetails);
        
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .expiresIn(60000L) // 1 minute
                .build();
    }
    
    public AuthResponse login(UserLoginRequest request) {
        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(
                request.getUsernameOrEmail(), 
                request.getUsernameOrEmail()
        ).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }
        
        // Generate JWT token
        UserDetails userDetails = loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(86400000L) // 24 hours in milliseconds
                .build();
    }
    
    /**
     * Kullanıcı profil bilgilerini döner. UserRepository'den kullanıcıyı çeker ve UserProfileResponse'a dönüştürür.
     * Giriş: username
     * Çıktı: UserProfileResponse (id, username, email, firstName, lastName, role, isActive, createdAt, updatedAt)
     * 
     * @param username Kullanıcı adı
     * @return UserProfileResponse (kullanıcı profil bilgileri)
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return UserProfileResponse.fromUser(user);
    }

    /**
     * Public profile (no email) for People directory.
     */
    public UserPublicDto getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return UserPublicDto.fromUser(user);
    }

    /**
     * Search active users by username or display name. Requires at least 2 characters.
     */
    public List<UserPublicDto> searchUsers(String query, int limit) {
        String q = query != null ? query.trim() : "";
        if (q.length() < 2) {
            return List.of();
        }
        int cap = Math.min(Math.max(limit, 1), 50);
        return userRepository.searchActiveUsers(q, PageRequest.of(0, cap)).stream()
                .map(UserPublicDto::fromUser)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserProfileResponse updateUserProfile(String username, UserProfileResponse updateRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // Update allowed fields
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new RuntimeException("Email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
        }
        
        User savedUser = userRepository.save(user);
        return UserProfileResponse.fromUser(savedUser);
    }
    
    /**
     * JWT token'ı validate eder. Token'dan username çıkarır, kullanıcıyı yükler ve token'ın geçerliliğini kontrol eder.
     * Giriş: token (JWT token string)
     * Çıktı: Token geçerliyse true, değilse false
     * 
     * @param token JWT token string
     * @return Token geçerliyse true, değilse false
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = loadUserByUsername(username);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Token'dan user ID'yi çıkarır. Token'dan username'i alır, kullanıcıyı bulur ve ID'sini döner.
     * Giriş: token (JWT token string)
     * Çıktı: User ID (Long) veya null (token geçersizse veya kullanıcı bulunamazsa)
     * 
     * @param token JWT token string
     * @return User ID veya null
     */
    public Long getUserIdFromToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Token'dan username'i çıkarır. JwtService ile token'dan subject (username) çıkarılır.
     * Giriş: token (JWT token string)
     * Çıktı: Username string veya null (token geçersizse)
     * 
     * @param token JWT token string
     * @return Username veya null
     */
    public String getUsernameFromToken(String token) {
        try {
            return jwtService.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
