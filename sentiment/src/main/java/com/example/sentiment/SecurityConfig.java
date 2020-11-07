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
			.httpBasic(withDefaults());
		return http.build();
	}

	@Bean
	WebMvcConfigurer webMvc() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/sentiment/**")
						.allowedOrigins("http://localhost:8080")
						.allowCredentials(true)
						.maxAge(0);
			}
		};
	}

	@Bean
	UserDetailsService users() {
		return new InMemoryUserDetailsManager(
				User.withDefaultPasswordEncoder()
						.username("bob")
						.password("tanzu")
						.authorities("app")
						.build()
		);
	}
}
