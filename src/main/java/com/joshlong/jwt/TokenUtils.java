package com.joshlong.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

abstract class TokenUtils {

	@SneakyThrows
	public static String buildTokenFor(JwtProperties properties, JWSSigner signer, Principal principal) {
		var now = Instant.now();
		var claims = new JWTClaimsSet.Builder()//
				.issuer(properties.getIssuer())//
				.audience(properties.getAudience())//
				.expirationTime(Date.from(now.plus(Duration.ofDays(2)))).issueTime(Date.from(now))//
				.subject(principal.getName())//
				.build();
		var jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
				new Payload(claims.toJSONObject()));
		jwsObject.sign(signer);
		return jwsObject.serialize();
	}

}
