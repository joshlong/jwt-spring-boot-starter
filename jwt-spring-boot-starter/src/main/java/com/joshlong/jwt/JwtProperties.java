package com.joshlong.jwt;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@RequiredArgsConstructor
@ConfigurationProperties("jwt")
public class JwtProperties {

	private final String tokenUrl = "/token";

	private final String issuer = "http://localhost:8080";

	private final String audience = "http://localhost:8080";

	private final String key;

}
