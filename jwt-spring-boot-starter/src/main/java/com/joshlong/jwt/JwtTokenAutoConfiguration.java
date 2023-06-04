package com.joshlong.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * JWT support that is 95% based on Rob Winch's epic code. If there are any faults, they
 * are surely Josh's.
 *
 * @author Rob Winch
 * @author Josh Long
 */

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
class JwtTokenAutoConfiguration {

	private final static Logger log = LoggerFactory.getLogger(JwtTokenAutoConfiguration.class);

	@Bean
	RSAKey rsaKey(JwtProperties properties) throws Exception {
		var key = properties.getKey();
		if (!StringUtils.hasText(key)) {
			key = UUID.randomUUID().toString();
			log.warn("the key is being generated automatically. It's recommended that you "
					+ "specify something (jwt.key) so that the key is stable across restarts");
		}
		return new RSAKeyGenerator(2048).keyID(key).generate();
	}

	@Bean
	RSASSASigner rsassaSigner(RSAKey rsaKey) throws JOSEException {
		return new RSASSASigner(rsaKey);
	}

}
