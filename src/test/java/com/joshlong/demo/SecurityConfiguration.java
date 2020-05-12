package com.joshlong.demo;

import com.joshlong.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static com.joshlong.jwt.JwtDsl.jwtDsl;

@Configuration
@RequiredArgsConstructor
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final JwtProperties properties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.apply(jwtDsl().tokenUrl(this.properties.getTokenUrl()));
	}

	@Bean
	UserDetailsService authentication() {
		var user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
		return new InMemoryUserDetailsManager(user);
	}

}
