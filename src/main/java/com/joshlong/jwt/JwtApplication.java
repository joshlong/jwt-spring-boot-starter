package com.joshlong.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * JWT support that is 95% based on Rob Winch's epic code. If there are any faults, they
 * are surely mine.
 *
 * @author Rob Winch
 * @author Josh Long
 */
@Configuration
@Log4j2
@EnableConfigurationProperties(JwtProperties.class)
class JwtTokenAutoConfiguration {

	@Bean
	JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
		return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
	}

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

	@Bean
	RouterFunction<ServerResponse> jwtTokenEndpoint(JwtProperties properties, JWSSigner signer) {
		return route()//
				.POST(properties.getTokenUrl(), serverRequest -> {
					Optional<Principal> pp = serverRequest.principal();
					Assert.isTrue(pp.isPresent(), "the principal must be non-null!");
					var principal = pp.get();
					var now = Instant.now();
					var claims = new JWTClaimsSet.Builder()//
							.issuer(properties.getIssuer())//
							.audience(properties.getAudience())//
							.expirationTime(Date.from(now.plus(Duration.ofDays(2))))//
							.issueTime(Date.from(now))//
							.subject(principal.getName())//
							.build();
					var jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
							new Payload(claims.toJSONObject()));
					jwsObject.sign(signer);
					var token = jwsObject.serialize();
					return ServerResponse.ok().body(token);
				})//
				.build();
	}

}
