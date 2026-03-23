package com.example.file_service.configurations;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<String> userRole = jwt.getClaim("user-group");

        if (userRole == null || !(userRole.size() !=0)) {
            return Collections.emptyList();
        }

        List<String> cleanedRoles = userRole.stream().map(role -> role.replace("/", "")) // Remove leading slash if present
                .collect(Collectors.toList());

        return cleanedRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}