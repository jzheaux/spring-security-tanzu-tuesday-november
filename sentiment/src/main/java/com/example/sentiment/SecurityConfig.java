package com.example.sentiment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(withDefaults())
			.authorizeRequests((authz) -> authz
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt(withDefaults())
			);
		return http.build();
	}

	@Bean
	WebMvcConfigurer webMvc() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/sentiment/**")
						.allowedOrigins("http://localhost:8080")
						.allowedHeaders("Authorization")
						.maxAge(0);
			}
		};
	}
}
