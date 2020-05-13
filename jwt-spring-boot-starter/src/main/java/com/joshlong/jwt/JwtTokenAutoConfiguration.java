package com.joshlong.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
class JwtTokenAutoConfiguration {

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
