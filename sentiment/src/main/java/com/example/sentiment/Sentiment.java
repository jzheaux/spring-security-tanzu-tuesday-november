package com.example.sentiment;

public class Sentiment {
	Integer sentiment = 0;

	public Integer getSentiment() {
		return sentiment;
	}

	Sentiment up() {
		this.sentiment++;
		return this;
	}

	Sentiment down() {
		this.sentiment--;
		return this;
	}
}
