package com.joshlong.jwt;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.security.Principal;
import java.util.Optional;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
@AutoConfigureAfter(JwtTokenAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class WebmvcTokenEndpointAutoConfiguration {

	@Bean
	JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
		return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
	}

	@Bean
	RouterFunction<ServerResponse> jwtTokenServletEndpoint(JwtProperties properties, JWSSigner signer) {
		return route()//
				.POST(properties.getTokenUrl(), serverRequest -> {
					Optional<Principal> pp = serverRequest.principal();
					Assert.isTrue(pp.isPresent(), "the principal must be non-null!");
					var token = TokenUtils.buildTokenFor(properties, signer, pp.get());
					return ServerResponse.ok().body(token);
				})//
				.build();
	}

	@Configuration
	@RequiredArgsConstructor
	@Order(99)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	public static class WebmvcSecurityConfiguration extends WebSecurityConfigurerAdapter {

		private final JwtProperties properties;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.apply(Jwt.webmvcDsl(this.properties.getTokenUrl()));
		}

	}

}
