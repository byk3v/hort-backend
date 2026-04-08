package com.kubuci.hort.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakResourceRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final String clientId;

    public KeycloakResourceRoleConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> resAccess = jwt.getClaim("resource_access");
        if (resAccess == null)
            return List.of();
        Object client = resAccess.get(clientId);
        if (!(client instanceof Map<?, ?> m))
            return List.of();
        Object roles = m.get("roles");
        if (!(roles instanceof Collection<?> col))
            return List.of();
        return col.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }
}
