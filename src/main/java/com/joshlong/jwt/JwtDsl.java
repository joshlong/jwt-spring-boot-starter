package com.joshlong.jwt;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

/**
 * <p>
 * Supports a custom DSL for plugging <a href=
 * "https://docs.spring.io/spring-security/site/docs/5.4.0-M1/reference/html5/#jc-custom-dsls">
 * in JWT configuration for Servlet-based applications </a>.
 * </P>
 *
 * You can specify a few properties on the classpath and then pull in the JWT DSL like
 * this: <PRE>
 * init(HttpSecurity builder){
 *   builder.apply(jwtDsl().tokenUrl("/my-token-endpoint"));
 * }
 * </PRE>
 */
public class JwtDsl extends AbstractHttpConfigurer<JwtDsl, HttpSecurity> {

	private String tokenUrl = "/token";

	public JwtDsl tokenUrl(String url) {
		this.tokenUrl = url;
		return this;
	}

	@Override
	public void init(HttpSecurity builder) throws Exception {

		builder//
				.csrf(AbstractHttpConfigurer::disable)//
				.authorizeRequests(ae -> ae.mvcMatchers(this.tokenUrl).authenticated())//
				.httpBasic(Customizer.withDefaults())//
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
	}

	public static JwtDsl jwtDsl() {
		return new JwtDsl();
	}

}
