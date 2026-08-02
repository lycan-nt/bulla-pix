package com.bullla.pix.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DemoTokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final String demoUser;
    private final String demoPassword;

    public DemoTokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds,
            @Value("${app.security.demo.username:demo}") String demoUser,
            @Value("${app.security.demo.password:demo}") String demoPassword
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.demoUser = demoUser;
        this.demoPassword = demoPassword;
    }

    public String issueToken(String username, String password) {
        if (!demoUser.equals(username) || !demoPassword.equals(password)) {
            throw new InvalidDemoCredentialsException();
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bullla-pix-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(username)
                .claim("scope", "pix")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
