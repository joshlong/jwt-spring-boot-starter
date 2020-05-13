package com.joshlong.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@AutoConfigureAfter(JwtTokenAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class WebfluxTokenEndpointAutoConfiguration {

	@Bean
	RouterFunction<ServerResponse> jwtTokenWebfluxEndpoint(JwtProperties properties, JWSSigner signer) {
		var scheduler = Schedulers.boundedElastic();
		return route()//
				.POST(properties.getTokenUrl(), request -> request.principal().flatMap(principal -> {
					Mono<String> tokenMono = Mono
							.fromCallable(() -> TokenUtils.buildTokenFor(properties, signer, principal))
							.publishOn(scheduler);
					return ServerResponse.ok().body(tokenMono, String.class);

				}))//
				.build();
	}

}
