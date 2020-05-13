package com.joshlong.demo.servlet;

import com.joshlong.jwt.Jwt;
import com.joshlong.jwt.JwtProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Log4j2
class WebfluxDemoApplicationTest {

	@Test
	public void webflux() {
		var args = new String[] { "spring.main.web-application-type=servlet" };
		SpringApplication.run(DemoApplication.class, args);
		var username = "user";
		var token = new RestTemplateBuilder()//
				.basicAuthentication(username, "password")//
				.build()//
				.postForEntity("http://localhost:8080/token", null, String.class)//
				.getBody();
		var response = new RestTemplateBuilder()//
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)//
				.build()//
				.getForEntity("http://localhost:8080/greetings", Greeting.class)//
				.getBody();
		log.info("token: " + token);
		log.info("response: " + response);
		Assertions.assertEquals(response.getGreeting(), "Hello " + username + "!");
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Greeting {

		private String greeting;

	}

}

@SpringBootApplication
class DemoApplication {

}

@Configuration
@RequiredArgsConstructor
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final JwtProperties properties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.apply(Jwt.servletJwtDsl(this.properties.getTokenUrl()));
	}

	@Bean
	UserDetailsService authentication() {
		var user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
		return new InMemoryUserDetailsManager(user);
	}

}

@RestController
class MessageController {

	@GetMapping("/greetings")
	Map<String, String> greet(Principal principal) {
		return singletonMap("greeting", "Hello " + principal.getName() + "!");
	}

}
