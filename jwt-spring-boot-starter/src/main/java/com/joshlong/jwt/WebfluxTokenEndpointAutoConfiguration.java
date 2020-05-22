package com.joshlong.jwt;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Log4j2
@Order(99)
@Configuration
@AutoConfigureAfter(JwtTokenAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class WebfluxTokenEndpointAutoConfiguration {

	@Bean
	NimbusReactiveJwtDecoder reactiveJwtDecoder(RSAKey rsaKey) throws Exception {
		return NimbusReactiveJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
	}

	@Bean
	SecurityWebFilterChain configureAuthenticationForTokenEndpoint(JwtProperties properties,
			ServerHttpSecurity httpSecurity) {
		return Jwt.webfluxDsl(httpSecurity, properties.getTokenUrl()).build();
	}

	@Bean
	RouterFunction<ServerResponse> jwtTokenWebfluxEndpoint(JwtProperties properties, JWSSigner signer) {
		var scheduler = Schedulers.boundedElastic();
		if (log.isDebugEnabled()) {
			log.debug("configuring the JWT token endpoint as '" + properties.getTokenUrl().trim() + "'");
		}
		return route().POST(properties.getTokenUrl().trim(), request -> request//
				.principal()//
				.flatMap(principal -> {//
					Mono<String> tokenMono = Mono.fromCallable(() -> {
						var tokenFor = TokenUtils.buildTokenFor(properties, signer, principal);
						if (log.isDebugEnabled()) {
							log.debug("the resulting token is " + tokenFor);
						}
						return tokenFor;
					}).subscribeOn(scheduler);
					return ServerResponse.ok().body(tokenMono, String.class);
				}))//
				.build();
	}

}
