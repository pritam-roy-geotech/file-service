package com.example.file_service.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TestJwtUtil {

    private static final String SECRET =
            "test-123456789-test-123456789-test";

    // Full control — pass whatever you need per test
    public static String generateToken(
            String username,
            String email,
            List<String> realmRoles,
            List<String> userGroups) {

        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8));

        long now = System.currentTimeMillis() / 1000;

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer("http://localhost:8080/realms/master")
                .audience().add("account").and()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date(now * 1000))
                .expiration(new Date((now + 3600) * 1000))
                .claim("typ", "Bearer")
                .claim("azp", "spring-client")
                .claim("sid", UUID.randomUUID().toString())
                .claim("acr", "1")
                .claim("allowed-origins", List.of("http://localhost:8080"))
                .claim("realm_access", Map.of("roles", realmRoles))
                .claim("resource_access", Map.of(
                        "account", Map.of("roles", List.of(
                                "manage-account",
                                "manage-account-links",
                                "view-profile"
                        ))
                ))
                .claim("scope", "userUUID email profile")
                .claim("email_verified", false)
                .claim("name", username)
                .claim("user-group", userGroups)
                .claim("preferred_username", email)
                .claim("given_name", username.split(" ")[0])
                .claim("family_name", username.split(" ").length > 1
                        ? username.split(" ")[1] : "")
                .claim("email", email)
                .signWith(key)
                .compact();
    }

    // Convenience methods for common test scenarios
    public static String generateAdminToken() {
        return generateToken(
                "Anubhab3 kar",
                "anubhab20002003@gmail.com",
                List.of("default-roles-master", "offline_access", "uma_authorization"),
                List.of("/ADMIN")
        );
    }

    public static String generateUserToken() {
        return generateToken(
                "Test User",
                "testuser@gmail.com",
                List.of("default-roles-master"),
                List.of("/USER")
        );
    }

    public static String generateExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8));

        long now = System.currentTimeMillis() / 1000;

        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer("http://localhost:8080/realms/master")
                .issuedAt(new Date((now - 7200) * 1000))   // 2 hrs ago
                .expiration(new Date((now - 3600) * 1000)) // expired 1 hr ago
                .claim("realm_access", Map.of("roles", List.of("default-roles-master")))
                .signWith(key)
                .compact();
    }
}