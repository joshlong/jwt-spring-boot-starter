package com.joshlong.jwt;

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
 *
 * Most of this code works because I stole code, time and ideas from Spring Security team
 * members Rob Winch and Josh Cummings. <EM>Thank you!</EM>
 *
 * @author Rob Winch
 * @author Josh Long
 * @author Josh Cummings
 */
public class Jwt {

	private static final String DEFAULT_TOKEN_URL = "/token";

	public static ServerHttpSecurity webfluxDsl(ServerHttpSecurity builder) {
		return webfluxDsl(builder, DEFAULT_TOKEN_URL);
	}

	public static ServerHttpSecurity webfluxDsl(ServerHttpSecurity builder, String tokenUrl) {
		return builder.securityMatcher(new PathPatternParserServerWebExchangeMatcher(tokenUrl))
				.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(ae -> ae.anyExchange().authenticated())//
				.httpBasic(Customizer.withDefaults());

	}

	public static ServletJwtDsl servletJwtDsl() {
		return new ServletJwtDsl().tokenUrl(DEFAULT_TOKEN_URL);
	}

	public static ServletJwtDsl servletJwtDsl(String loginUrl) {
		return new ServletJwtDsl().tokenUrl(loginUrl);
	}

	public static class ServletJwtDsl extends AbstractHttpConfigurer<ServletJwtDsl, HttpSecurity> {

		private String tokenUrl;

		public ServletJwtDsl tokenUrl(String url) {
			this.tokenUrl = url;
			return this;
		}

		@Override
		public void init(HttpSecurity builder) throws Exception {
			builder//
					.requestMatchers(c -> c.mvcMatchers(this.tokenUrl)).csrf(AbstractHttpConfigurer::disable)//
					.authorizeRequests(ae -> ae//
							.mvcMatchers(this.tokenUrl).authenticated()//
					)//
					.httpBasic(Customizer.withDefaults())//
			;
		}

	}

}
