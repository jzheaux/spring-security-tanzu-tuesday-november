package com.example.app;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

@Component
public class CsrfResponseHeaderWebFilter implements WebFilter {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		HttpHeaders headers = exchange.getResponse().getHeaders();
		String key = CsrfToken.class.getName();
		Mono<CsrfToken> csrfToken = exchange.getAttributes().containsKey(key) ?
				exchange.getAttribute(key) : Mono.empty();
		return csrfToken
				.doOnSuccess(token -> headers.set(token.getHeaderName(), token.getToken()))
				.then(chain.filter(exchange));
	}
}
