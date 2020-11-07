package com.example.sentiment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentiment")
public class SentimentController {
	private final Map<String, Sentiment> sentiment = new ConcurrentHashMap<>();

	@GetMapping
	@PreAuthorize("hasAuthority('SCOPE_sentiment.read')")
	public EntityModel<Sentiment> read(Authentication authentication) {
		return EntityModel.of(read(authentication.getName()));
	}

	@PostMapping("/up")
	@PreAuthorize("hasAuthority('SCOPE_sentiment.write')")
	public EntityModel<Sentiment> up(Authentication authentication) {
		Sentiment sentiment = read(authentication.getName());
		return EntityModel.of(sentiment.up());
	}

	@PostMapping("/down")
	@PreAuthorize("hasAuthority('SCOPE_sentiment.write')")
	public EntityModel<Sentiment> down(Authentication authentication) {
		Sentiment sentiment = read(authentication.getName());
		return EntityModel.of(sentiment.down());
	}

	private Sentiment read(String name) {
		return this.sentiment.computeIfAbsent(name, (k) -> new Sentiment(name));
	}
}
