package com.example.app;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

@Configuration(proxyBeanMethods = false)
public class RoutingConfig {
	@Bean
	public ReactiveOAuth2AuthorizedClientManager manager(ReactiveClientRegistrationRepository clients,
														 ServerOAuth2AuthorizedClientRepository authz) {
		return new DefaultReactiveOAuth2AuthorizedClientManager(clients, authz);
	}

	@Bean
	public TokenRelayGatewayFilterFactory tokenRelay(ReactiveOAuth2AuthorizedClientManager manager) {
		return new TokenRelayGatewayFilterFactory(manager);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, TokenRelayGatewayFilterFactory tokenRelay) {
		//@formatter:off
		return builder.routes()
				.route("sentiment", r -> r.path("/sentiment/**")
						.filters(f -> f.filter(tokenRelay.apply()))
						.uri("http://localhost:8180/sentiment"))
				.build();
		//@formatter:on
	}
}
