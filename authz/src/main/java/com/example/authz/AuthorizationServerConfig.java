package com.example.authz;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.keys.KeyManager;
import org.springframework.security.crypto.keys.StaticKeyGeneratingKeyManager;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.JoseHeader;
import org.springframework.security.oauth2.jose.jws.NimbusJwsEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationAttributeNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class AuthorizationServerConfig {

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
		UserDetails user = User.withDefaultPasswordEncoder()
				.username("jon")
				.password("password")
				.roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}
	// @formatter:on

	@Bean
	@Order(1)
	SecurityFilterChain filterChain(HttpSecurity http,
									RegisteredClientRepository clients,
									KeyManager keyManager) throws Exception {
		OAuth2AuthorizationService service = new InMemoryOAuth2AuthorizationService();
		OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
				new OAuth2AuthorizationServerConfigurer<>();
		RequestMatcher[] endpointMatchers = authorizationServerConfigurer
				.getEndpointMatchers().toArray(new RequestMatcher[0]);

		http
				.requestMatcher(new OrRequestMatcher(endpointMatchers))
				.authorizeRequests(authorizeRequests ->
						authorizeRequests.anyRequest().authenticated()
				)
				.formLogin(withDefaults())
				.csrf(csrf -> csrf.ignoringRequestMatchers(endpointMatchers))
				.apply(authorizationServerConfigurer)
					.authorizationService(service);

		JwtEncoder encoder = new NimbusJwsEncoder(keyManager);

		authorizationServerConfigurer
				.addObjectPostProcessor(new ObjectPostProcessor<OAuth2AuthorizationEndpointFilter>() {
					@Override
					public <O extends OAuth2AuthorizationEndpointFilter> O postProcess(O object) {
						return (O) new WithImplicit(clients, service, encoder);
					}
				});
		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain login(HttpSecurity http) throws Exception {
		http
				.authorizeRequests((authz) -> authz
						.anyRequest().authenticated()
				)
				.formLogin(withDefaults())
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		return http.build();
	}

	// this is for educational purposes to show how (the now-deprecated) implicit grant behaved. NOT SECURE
	private static class WithImplicit extends OAuth2AuthorizationEndpointFilter {
		private final RegisteredClientRepository clients;
		private final OAuth2AuthorizationService service;
		private final JwtEncoder encoder;
		private final RequestMatcher matcher;
		private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

		public WithImplicit(RegisteredClientRepository registeredClientRepository, OAuth2AuthorizationService authorizationService, JwtEncoder encoder) {
			super(registeredClientRepository, authorizationService);
			this.clients = registeredClientRepository;
			this.service = authorizationService;
			this.encoder = encoder;
			this.matcher = new AntPathRequestMatcher(DEFAULT_AUTHORIZATION_ENDPOINT_URI, HttpMethod.GET.name());
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws ServletException, IOException {
			String responseType = request.getParameter(OAuth2ParameterNames.RESPONSE_TYPE);
			if (this.matcher.matches(request) && "token".equals(responseType)) {
				Authentication principal = SecurityContextHolder.getContext().getAuthentication();
				if (!isPrincipalAuthenticated(principal)) {
					// Pass through the chain with the expectation that the authentication process
					// will commence via AuthenticationEntryPoint
					chain.doFilter(request, response);
					return;
				}
				String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
				RegisteredClient client = this.clients.findByClientId(clientId);

				OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(client)
						.principalName(principal.getName());

				URL issuer = null;
				try {
					issuer = URI.create("https://oauth2.provider.com").toURL();
				} catch (MalformedURLException e) {
				}

				Instant issuedAt = Instant.now();
				Instant expiresAt = issuedAt.plus(1, ChronoUnit.HOURS);
				Set<String> authorizedScopes = client.getScopes();

				JoseHeader joseHeader = JoseHeader.withAlgorithm(SignatureAlgorithm.RS256).build();
				JwtClaimsSet jwtClaimsSet = JwtClaimsSet.withClaims()
						.issuer(issuer)
						.subject(principal.getName())
						.audience(Collections.singletonList(client.getClientId()))
						.issuedAt(issuedAt)
						.expiresAt(expiresAt)
						.notBefore(issuedAt)
						.claim(OAuth2ParameterNames.SCOPE, authorizedScopes)
						.build();
				Jwt jwt = this.encoder.encode(joseHeader, jwtClaimsSet);
				OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
						jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaim(OAuth2ParameterNames.SCOPE));

				OAuth2Authorization authorization = builder
						.accessToken(accessToken)
						.attribute(OAuth2AuthorizationAttributeNames.ACCESS_TOKEN_ATTRIBUTES, jwt)
						.attribute(OAuth2AuthorizationAttributeNames.AUTHORIZED_SCOPES, authorizedScopes)
						.build();

				this.service.save(authorization);

				String state = request.getParameter(OAuth2ParameterNames.STATE);
				sendAuthorizationResponse(request, response,
						CollectionUtils.firstElement(client.getRedirectUris()), jwt.getTokenValue(), state);
				return;
			}
			super.doFilterInternal(request, response, chain);
		}

		private static boolean isPrincipalAuthenticated(Authentication principal) {
			return principal != null &&
					!AnonymousAuthenticationToken.class.isAssignableFrom(principal.getClass()) &&
					principal.isAuthenticated();
		}

		private void sendAuthorizationResponse(HttpServletRequest request, HttpServletResponse response,
											   String redirectUri, String token, String state) throws IOException {

			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromUriString(redirectUri)
					.queryParam(OAuth2ParameterNames.ACCESS_TOKEN, token);
			if (StringUtils.hasText(state)) {
				uriBuilder.queryParam(OAuth2ParameterNames.STATE, state);
			}
			this.redirectStrategy.sendRedirect(request, response, uriBuilder.toUriString());
		}
	}
}