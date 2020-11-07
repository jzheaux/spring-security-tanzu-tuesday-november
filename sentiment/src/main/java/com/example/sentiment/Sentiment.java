package com.example.sentiment;

import java.util.concurrent.atomic.AtomicInteger;

public class Sentiment {
	private String name;
	private AtomicInteger sentiment = new AtomicInteger(0);

	public Sentiment(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Integer getSentiment() {
		return sentiment.get();
	}

	Sentiment up() {
		this.sentiment.incrementAndGet();
		return this;
	}

	Sentiment down() {
		this.sentiment.decrementAndGet();
		return this;
	}
}
