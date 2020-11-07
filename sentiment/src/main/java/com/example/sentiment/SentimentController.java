package com.example.sentiment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentiment")
public class SentimentController {
	private final Map<String, Sentiment> sentiment = new ConcurrentHashMap<>();
	private final String name = "bob";

	@GetMapping
	public Sentiment read() {
		return read(this.name);
	}

	@PostMapping("/up")
	public Sentiment up() {
		Sentiment sentiment = read(this.name);
		return sentiment.up();
	}

	@PostMapping("/down")
	public Sentiment down() {
		Sentiment sentiment = read(this.name);
		return sentiment.down();
	}

	private Sentiment read(String name) {
		return this.sentiment.computeIfAbsent(name, (k) -> new Sentiment(name));
	}
}
