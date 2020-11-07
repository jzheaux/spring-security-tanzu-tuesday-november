package com.example.sentiment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	public Sentiment read(Authentication authentication) {
		return read(authentication.getName());
	}

	@PostMapping("/up")
	public Sentiment up(Authentication authentication) {
		Sentiment sentiment = read(authentication.getName());
		return sentiment.up();
	}

	@PostMapping("/down")
	public Sentiment down(Authentication authentication) {
		Sentiment sentiment = read(authentication.getName());
		return sentiment.down();
	}

	private Sentiment read(String name) {
		return this.sentiment.computeIfAbsent(name, (k) -> new Sentiment(name));
	}
}
