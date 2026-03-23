package com.example.file_service.configurations;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // no sessions, JWT only
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE,"/api/v1/**").hasRole("ADMIN")  // open endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasAnyRole("ADMIN","USER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

        // Keycloak puts roles inside "realm_access.roles" in the JWT
//        converter.setAuthoritiesClaimName("user-group");
//        converter.setAuthorityPrefix("/");

        KeycloakRoleConverter keycloakRoleConverter = new KeycloakRoleConverter();
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        return jwtConverter;
    }

    @Bean
    @Profile("test")
    public JwtDecoder jwtDecoder() {
        String jwtSecret = "test-123456789-test-123456789-test";
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return NimbusJwtDecoder.withSecretKey(key).build();

    }
}
