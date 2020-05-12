package com.joshlong.jwt;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

/**
 * @author Rob Winch
 * @author Josh Long
 * <p>
 * https://docs.spring.io/spring-security/site/docs/5.4.0-M1/reference/html5/#jc-custom-dsls
 */
public class JwtDsl extends AbstractHttpConfigurer<JwtDsl, HttpSecurity> {

	private String tokenUrl = "/token";

	public JwtDsl tokenUrl(String url) {
		this.tokenUrl = url;
		return this;
	}

	@Override
	public void init(HttpSecurity builder) throws Exception {

		builder.csrf(AbstractHttpConfigurer::disable)
				// .authorizeRequests(ae -> ae.mvcMatchers(this.tokenUrl).authenticated())
				.authorizeRequests(ae -> ae.anyRequest().authenticated()).httpBasic(Customizer.withDefaults())
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
	}

	public static JwtDsl jwtDsl() {
		return new JwtDsl();
	}

}
