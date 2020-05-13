package com.joshlong.demo.reactive;

import com.joshlong.jwt.Jwt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Log4j2
class ReactiveDemoApplicationTest {

	@Test
	public void reactive() throws Exception {

		var args = new String[] { "spring.main.web-application-type=reactive" };
		SpringApplication.run(DemoApplication.class, args);
		var username = "user";
		WebClient tokenClient = WebClient.builder()
				.filter(ExchangeFilterFunctions.basicAuthentication(username, "password")).build();
		/*
		 * tokenClient .post() .uri("http://localhost:8080/token") .retrieve()
		 * .bodyToFlux(String.class) .subscribe(log::info);
		 */
		System.in.read();

		/*
		 * var token = new RestTemplateBuilder()// .basicAuthentication(username,
		 * "password")// .build()// .postForEntity("http://localhost:8080/token", null,
		 * String.class)// .getBody(); var response = new RestTemplateBuilder()//
		 * .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)// .build()//
		 * .getForEntity("http://localhost:8080/greetings", Greeting.class)// .getBody();
		 */
		// log.info("token: " + token);
		// log.info("response: " + response);
		// Assertions.assertEquals(response.getGreeting(), "Hello " + username + "!");
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Greeting {

		private String greeting;

	}

}

@Configuration
class SecurityConfiguration {

	/*
	 * @Bean SecurityWebFilterChain authorization(ServerHttpSecurity http) {
	 * http.csrf().disable(); http.httpBasic(); http.authorizeExchange()
	 * .pathMatchers("/proxy").authenticated() .anyExchange().permitAll(); return
	 * http.build(); }
	 */

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity httpSecurity) {
		return Jwt.webfluxDsl(httpSecurity).build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(
				User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build());
	}

}

@SpringBootApplication
class DemoApplication {

}

@RestController
class MessageController {

	@GetMapping("/greetings")
	Mono<Map<String, String>> greet(Principal principal) {
		return Mono.just(singletonMap("greeting", "Hello " + principal.getName() + "!"));
	}

}