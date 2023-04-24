package com.joshlong.jwt;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

/**
 * <p>
 * Supports a custom DSL for plugging <a href=
 * "https://docs.spring.io/spring-security/site/docs/5.4.0-M1/reference/html5/#jc-custom-dsls">
 * in JWT configuration for Servlet-based </a> or Spring Webflux-based applications.
 * </P>
 * <p>
 * Most of this code works because I stole code, time and ideas from Spring Security team
 * members Rob Winch and Josh Cummings. <EM>Thank you!</EM>
 *
 * @author Rob Winch
 * @author Josh Long
 * @author Josh Cummings
 */
@Log4j2
public class Jwt {

	public static ServerHttpSecurity webfluxDsl(ServerHttpSecurity builder, String tokenUrl) {
		log.info("configuring for " + tokenUrl);
		return builder//
			.securityMatcher(new PathPatternParserServerWebExchangeMatcher(tokenUrl))//
			.cors(Customizer.withDefaults())//
			.csrf(ServerHttpSecurity.CsrfSpec::disable)//
			.authorizeExchange(ae -> ae.pathMatchers(tokenUrl).authenticated())//
			.httpBasic(Customizer.withDefaults());

	}

	public static WebmvcDsl webmvcDsl(String loginUrl) {
		return new WebmvcDsl().tokenUrl(loginUrl);
	}

	public static class WebmvcDsl extends AbstractHttpConfigurer<WebmvcDsl, HttpSecurity> {

		private String tokenUrl;

		public WebmvcDsl tokenUrl(String url) {
			this.tokenUrl = url;
			return this;
		}

		@Override
		public void init(HttpSecurity builder) throws Exception {
			builder//
				.requestMatchers(c -> c.mvcMatchers(this.tokenUrl))//
				.csrf(AbstractHttpConfigurer::disable)//
				.cors(Customizer.withDefaults())//
				.authorizeRequests(ae -> ae.mvcMatchers(this.tokenUrl).authenticated())//
				.httpBasic(Customizer.withDefaults())//
			;
		}

	}

}
