package com.financas.tema1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
				.headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/h2-console/**", "/api/**").permitAll()
						.anyRequest().authenticated())
				.build();
	}
}
