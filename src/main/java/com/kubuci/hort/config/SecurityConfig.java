package com.kubuci.hort.config;

import java.util.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        final var jwtAuthConverter = getJwtAuthenticationConverter();

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // .requestMatchers("/public/**").permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    private static JwtAuthenticationConverter getJwtAuthenticationConverter() {
        var scopes = new JwtGrantedAuthoritiesConverter();
        var realmRoles = new KeycloakRealmRoleConverter();
        var clientRoles = new KeycloakResourceRoleConverter("hort-rest-api");

        var jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var all = new ArrayList<>(scopes.convert(jwt));
            all.addAll(realmRoles.convert(jwt));
            all.addAll(clientRoles.convert(jwt));
            return all;
        });
        return jwtAuthConverter;
    }
}
