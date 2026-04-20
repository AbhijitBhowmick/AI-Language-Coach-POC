package com.coach.identity;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthenticationResponse> oauth2Success(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUser) {
            var user = new User();
            user.setEmail(customUser.getEmail());
            user.setId(customUser.getId());
            return ResponseEntity.ok(authenticationService.oauth2Success(user));
        }
        return ResponseEntity.ok().build();
    }
}