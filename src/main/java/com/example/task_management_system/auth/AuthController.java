package com.example.task_management_system.auth;


import com.example.task_management_system.auth.dto.PostTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtEncoder jwtEncoder;

    public AuthController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @GetMapping
    public String login(Authentication user){

        return "Hello "+user.getName()+" you are a "+user.getAuthorities();
    }

    @PostMapping("/token")
    public ResponseEntity<PostTokenResponse> token(Authentication authentication){


        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Instant now = Instant.now();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(60, ChronoUnit.SECONDS))
                .claim("scope", authorities)
                .build();



        return ResponseEntity.status(200)
                .body(new PostTokenResponse(jwtEncoder.encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue()));

    }


}