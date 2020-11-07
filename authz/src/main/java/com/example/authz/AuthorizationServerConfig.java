package com.example.authz;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.keys.KeyManager;
import org.springframework.security.crypto.keys.StaticKeyGeneratingKeyManager;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AuthorizationServerConfig {

	@Bean
	@Order(1)
	SecurityFilterChain oauth2Endpoints(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerSecurity.applyDefaultConfiguration(http);
		return http.build();
	}

	// @formatter:off
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("sentiment-client")
				.clientSecret("secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("http://localhost:8080/login/oauth2/code/spring")
				.scope("sentiment.read")
				.scope("sentiment.write")
				.clientSettings((settings) -> settings.requireUserConsent(true))
				.build();
		return new InMemoryRegisteredClientRepository(registeredClient);
	}
	// @formatter:on

	@Bean
	public KeyManager keyManager() {
		return new StaticKeyGeneratingKeyManager();
	}

	// @formatter:off
	@Bean
	public UserDetailsService users() {
		UserDetails bob = User.withDefaultPasswordEncoder()
				.username("bob")
				.password("tanzu")
				.authorities("app")
				.build();
		UserDetails tiffany = User.withDefaultPasswordEncoder()
				.username("tiffany")
				.password("tanzu")
				.authorities("app")
				.build();
		UserDetails paul = User.withDefaultPasswordEncoder()
				.username("paul")
				.password("tanzu")
				.authorities("app")
				.build();
		return new InMemoryUserDetailsManager(bob, tiffany, paul);
	}
	// @formatter:on

	@Bean
	@Order(2)
	SecurityFilterChain appEndpoints(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests((authz) -> authz.anyRequest().authenticated())
			.formLogin(Customizer.withDefaults())
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt(Customizer.withDefaults()));
		return http.build();
		// @formatter:on
	}
}