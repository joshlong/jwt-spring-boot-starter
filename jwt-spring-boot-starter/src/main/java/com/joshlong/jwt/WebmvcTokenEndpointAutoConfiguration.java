package com.joshlong.jwt;

import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(JwtTokenAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class WebmvcTokenEndpointAutoConfiguration {

	@Bean
	ApplicationRunner applicationRunner() {
		return a -> LoggerFactory.getLogger(getClass()).info("we need to reimplement the MVC support!");
	}
	/*
	 * @Bean JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception { return
	 * NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build(); }
	 *
	 * @Bean RouterFunction<ServerResponse> jwtTokenServletEndpoint(JwtProperties
	 * properties, JWSSigner signer) { return route()// .POST(properties.tokenUrl(),
	 * serverRequest -> { Optional<Principal> pp = serverRequest.principal();
	 * Assert.isTrue(pp.isPresent(), "the principal must be non-null!"); var token =
	 * TokenUtils.buildTokenFor(properties, signer, pp.get()); return
	 * ServerResponse.ok().body(token); })// .build(); }
	 *
	 * @Configuration
	 *
	 * @RequiredArgsConstructor
	 *
	 * @Order(99)
	 *
	 * @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	 * public static class WebmvcSecurityConfiguration extends
	 * WebSecurityConfigurerAdapter {
	 *
	 * private final JwtProperties properties;
	 *
	 * @Override protected void configure(HttpSecurity http) throws Exception {
	 * http.apply(Jwt.webmvcDsl(this.properties.getTokenUrl())); }
	 *
	 * }
	 */

}
