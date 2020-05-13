package com.joshlong.jwt;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * <p>
 * Supports a custom DSL for plugging <a href=
 * "https://docs.spring.io/spring-security/site/docs/5.4.0-M1/reference/html5/#jc-custom-dsls">
 * in JWT configuration for Servlet-based </a> or Spring Webflux-based applications.
 * </P>
 */
public class Jwt {

	private static final String DEFAULT_TOKEN_URL = "/token";

	public static ServerHttpSecurity webfluxDsl(ServerHttpSecurity builder) {
		return webfluxDsl(builder, DEFAULT_TOKEN_URL);
	}

	public static ServerHttpSecurity webfluxDsl(ServerHttpSecurity builder, String tokenUrl) {
		return builder//
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(ae -> ae.pathMatchers(tokenUrl).authenticated()//
						.anyExchange().authenticated()//
				)//
				.httpBasic(Customizer.withDefaults())//
				.oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);
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
					.csrf(AbstractHttpConfigurer::disable)//
					.authorizeRequests(ae -> ae//
							.mvcMatchers(this.tokenUrl).authenticated()//
							.anyRequest().authenticated()//
					)//
					.httpBasic(Customizer.withDefaults())//
					.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		}

	}

}
