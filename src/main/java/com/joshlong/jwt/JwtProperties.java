package com.joshlong.jwt;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("jwt")
public class JwtProperties {

	private final String secret;

	private final String issuer;

	private final String type;

	private final String audience;

	private final String loginUrl;

}
