package com.example.sentiment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.filter.ForwardedHeaderFilter;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SentimentApplication {

	@Bean
	ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	public static void main(String[] args) {
		SpringApplication.run(SentimentApplication.class, args);
	}

}
