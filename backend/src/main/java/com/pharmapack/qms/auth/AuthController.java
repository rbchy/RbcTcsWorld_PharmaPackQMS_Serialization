package com.pharmapack.qms.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public record LoginRequest(String username, String password) {}

    public record LoginResponse(String token, Long userId, String username, String fullName, java.util.List<String> roles) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        if (req.username() == null || req.username().isBlank() || req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (BadCredentialsException e) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal.getUsername(), principal.getRoleNames(), principal.getId());
        return new LoginResponse(token, principal.getId(), principal.getUsername(), principal.getFullName(), principal.getRoleNames());
    }

    /** Lets the frontend re-validate a stored token on app load / page refresh without re-prompting for a password. */
    @GetMapping("/me")
    public LoginResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return new LoginResponse(null, principal.getId(), principal.getUsername(), principal.getFullName(), principal.getRoleNames());
    }
}
