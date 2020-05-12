package com.joshlong.jwt;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties("jwt")
class JwtProperties {

	private final String tokenUrl;

}
