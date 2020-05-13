package com.joshlong.jwt.servlet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.Assert;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Log4j2
@SpringBootApplication
public class DemoApplication {

	static final String USERNAME = "user";
	static final String PASSWORD = "password";

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void servlet() {
		var token = new RestTemplateBuilder()//
				.basicAuthentication(USERNAME, PASSWORD)//
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
		Assert.isTrue(response.getGreeting().equalsIgnoreCase("Hello " + USERNAME + "!"), "the strings must match!");
	}

	@Bean
	RouterFunction<ServerResponse> http() {
		return route().GET("/greetings", request -> {//
			var p = request.principal().get();
			var greeting = new Greeting("hello " + p.getName() + "!");
			return ok().body(greeting);
		}).build();
	}

	@Bean
	UserDetailsService authentication() {
		return new InMemoryUserDetailsManager(User.withDefaultPasswordEncoder()//
				.username(USERNAME)//
				.password(PASSWORD)//
				.roles("USER")//
				.build()//
		);
	}

	@Configuration
	public static class MyConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http

					.authorizeRequests(ae -> ae.anyRequest().authenticated())
					.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		}

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Greeting {

		private String greeting;

	}

}
