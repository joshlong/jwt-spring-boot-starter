package com.joshlong.jwt.webflux;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
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

import java.util.function.Predicate;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Log4j2
@SpringBootApplication
class DemoApplication {

	static final String USERNAME = "user";
	static final String PASSWORD = "password";

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// todo
	@EventListener(ApplicationReadyEvent.class)
	public void reactiveRunner() throws Exception {
		var root = "http://localhost:8080";

		WebClient //
				.builder()//
				.filter(ExchangeFilterFunctions.basicAuthentication(USERNAME, PASSWORD))//
				.build()//
				.post()//
				.uri(root + "/token")//
				.retrieve()//
				.bodyToMono(String.class).flatMap(tokenString -> WebClient//
						.builder()//
						.build()//
						.get()//
						.uri(root + "/greetings")//
						.headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenString))//
						.retrieve()//
						.bodyToMono(String.class))
				.doOnError(log::error).onErrorReturn(new Predicate<Throwable>() {
					@Override
					public boolean test(Throwable throwable) {
						log.info(throwable.toString());
						return true;
					}
				}, "NOPE!").subscribe(response -> log.info("the response is " + response));
	}

	@Bean
	RouterFunction<ServerResponse> myHttpEndpoints() {
		return route()//
				.GET("/greetings", request -> {
					log.info("returning the request " + request);
					return request//
							.principal()//
							.map(p -> {//
								log.info("greetings requested : " + p.getName());
								return new Greeting("hello " + p.getName() + "!");
							}).onErrorResume(ex -> Mono.just(new Greeting("NOOOO")))
							.flatMap(g -> ServerResponse.ok().bodyValue(g))
							.switchIfEmpty(Mono.error(new IllegalAccessError()));

				})//
				.build();
	}

	@Bean
	MapReactiveUserDetailsService authentication1() {
		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()//
				.username(USERNAME)//
				.password(PASSWORD)//
				.roles("USER")//
				.build()//
		);
	}

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity httpSecurity) {
		return httpSecurity//
				// .authorizeExchange(ae ->
				// ae.pathMatchers("/greetings").authenticated())//
				.oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)//
				.build();
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Greeting {

		private String greeting;

	}

}
