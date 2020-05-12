package com.joshlong.demo;

import com.joshlong.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static com.joshlong.jwt.JwtDsl.jwtDsl;
import static java.util.Collections.singletonMap;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@RestController
class MessageController {

	@GetMapping("/greetings")
	Map<String, String> greet(Principal principal) {
		return singletonMap("greeting", "Hello " + principal.getName() + "!");
	}

}

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