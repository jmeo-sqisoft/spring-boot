package com.stagora.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/** Common application settings; business-specific properties are omitted. */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Cors cors = new Cors();

	@Getter
	@Setter
	public static class Cors {

		private List<String> allowedOriginPatterns = List.of(
			"http://localhost",
			"http://localhost:*",
			"http://127.0.0.1",
			"http://127.0.0.1:*",
			"http://[::1]",
			"http://[::1]:*");
	}
}
