package com.joshlong.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
class JwtTokenAutoConfiguration {

	@Bean
	JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
		return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
	}

	@Bean
	RSAKey rsaKey() throws Exception {
		return new RSAKeyGenerator(2048).keyID("123").generate();
	}

	@Bean
	RSASSASigner rsassaSigner(RSAKey rsaKey) throws JOSEException {
		return new RSASSASigner(rsaKey);
	}

	@Bean
	RouterFunction<ServerResponse> jwtTokenEndpoint(JWSSigner signer) {
		return route().POST("/token", serverRequest -> {
			Optional<Principal> pp = serverRequest.principal();
			Assert.isTrue(pp.isPresent(), "the principal must be non-null!");
			var principal = pp.get();
			var now = Instant.now();
			var claims = new JWTClaimsSet.Builder().issuer("http://localhost:8080").audience("http://localhost:8080")
					.expirationTime(Date.from(now.plus(Duration.ofDays(2)))).issueTime(Date.from(now))
					.subject(principal.getName()).build();
			var jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
					new Payload(claims.toJSONObject()));
			jwsObject.sign(signer);
			var token = jwsObject.serialize();
			return ServerResponse.ok().body(token);
		}).build();
	}

}

/*
 * @RestController class MessageController {
 *
 * @GetMapping("/") public String message(Principal principal) { return "Hello " +
 * principal.getName() + "!"; } }
 */
