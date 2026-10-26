package com.seegene.local.base.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
