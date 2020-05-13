package com.joshlong.jwt.reactive;

import com.joshlong.jwt.Jwt;
import com.joshlong.jwt.JwtProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Log4j2
@SpringBootApplication
class DemoApplication {

	static final String USERNAME = "user";
	static final String PASSWORD = "password";

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void reactive() {
		Mono<String> token = WebClient //
				.builder()//
				.filter(ExchangeFilterFunctions.basicAuthentication(USERNAME, PASSWORD))//
				.build()//
				.post()//
				.uri("http://localhost:8080/token")//
				.retrieve()//
				.bodyToMono(String.class);
		token.subscribe(log::info);
		Mono<Greeting> greetingPublisher = token.flatMap(token1 -> {
			log.info("the token is " + token1);
			return WebClient.builder().build().get().uri("http://localhost:8080/greetings")
					.headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + token1)).retrieve()
					.bodyToMono(Greeting.class);
		});
		// greetingPublisher.subscribe(log::info);

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Greeting {

		private String greeting;

	}

	@Bean
	RouterFunction<ServerResponse> http() {
		return route()//
				.GET("/greetings",
						request -> request.principal().flatMap(
								p -> ok().body(singletonMap("greetings", "hello " + p.getName() + "!"), Map.class)))//
				.build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()//
				.username(USERNAME)//
				.password(PASSWORD)//
				.roles("USER")//
				.build()//
		);
	}

	@Bean
	SecurityWebFilterChain authorization(JwtProperties properties, ServerHttpSecurity httpSecurity) {
		return Jwt.webfluxDsl(httpSecurity, properties.getTokenUrl()).build();
	}

}