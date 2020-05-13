package com.joshlong.demo.reactive;

import com.joshlong.jwt.Jwt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

class Credentials {

	public static final String USERNAME = "user";

	public static final String PASSWORD = "password";

}

@Log4j2
class ReactiveDemoApplicationTest {

	@RequiredArgsConstructor
	static class TokenExchangeFilterFunction implements ExchangeFilterFunction {

		private final Mono<String> token;

		@Override
		public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
			return this.token.flatMap(t -> next.exchange(ClientRequest.from(request)
					.headers((headers) -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + t)).build()));
		}

	}

	@Test
	public void reactive() throws Exception {

		var args = new String[] { "spring.main.web-application-type=reactive" };
		SpringApplication.run(DemoApplication.class, args);
		Mono<String> token = WebClient //
				.builder()//
				.filter(ExchangeFilterFunctions.basicAuthentication(Credentials.USERNAME, Credentials.PASSWORD))//
				.build()//
				.post()//
				.uri("http://localhost:8080/token")//
				.retrieve()//
				.bodyToMono(String.class);
		token.subscribe(log::info);
		Mono<Greeting> greetingPublisher = token.flatMap(this::given);
		// greetingPublisher.subscribe(log::info);
		System.in.read();
	}

	private Mono<Greeting> given(String token) {
		log.info("the token is " + token);
		return WebClient.builder().build().get().uri("http://localhost:8080/greetings")
				.headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + token)).retrieve()
				.bodyToMono(Greeting.class);
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Greeting {

		private String greeting;

	}

}

@Configuration
class ReactiveSecurityConfiguration {

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity httpSecurity) {
		return Jwt.webfluxDsl(httpSecurity).build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {

		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()//
				.username(Credentials.USERNAME)//
				.password(Credentials.PASSWORD)//
				.roles("USER")//
				.build()//
		);
	}

}

@SpringBootApplication
class DemoApplication {

}

@Log4j2
@Configuration
class HttpConfiguration {

	@Bean
	RouterFunction<ServerResponse> http() {
		return route().GET("/greetings", request -> request.principal().flatMap(this::from)).build();
	}

	private Mono<ServerResponse> from(Principal p) {
		log.info("new request : " + p.getName());
		var map = Collections.singletonMap("greetings", "hello " + p.getName() + "!");
		return ServerResponse.ok().body(map, Map.class);
	}

}