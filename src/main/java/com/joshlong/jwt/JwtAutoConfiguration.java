package com.joshlong.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@RequiredArgsConstructor
@Order(Integer.MAX_VALUE - 100)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration extends WebSecurityConfigurerAdapter {

	private final JwtProperties properties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests(ae -> ae //
				.mvcMatchers(this.properties.getLoginUrl()).permitAll() //
				.anyRequest().authenticated() //
		)//
				.addFilter(this.jwtAuthenticationFilter()) //
				.addFilter(this.jwtAuthorizationFilter());
	}

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
		return new JwtAuthenticationFilter(this.authenticationManager(), this.properties.getAudience(),
				this.properties.getIssuer(), this.properties.getSecret(), this.properties.getType(),
				this.properties.getLoginUrl());
	}

	@Bean
	JwtAuthorizationFilter jwtAuthorizationFilter() throws Exception {
		return new JwtAuthorizationFilter(this.authenticationManager(), this.properties.getSecret());
	}

}
