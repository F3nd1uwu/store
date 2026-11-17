package ru.web.store.inventory.infrastructure.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class JwtSecurityConfig {
  @Bean
  JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
    return NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a -> a.requestMatchers("/health").permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(o -> o.jwt(j -> {}))
        .build();
  }
}
