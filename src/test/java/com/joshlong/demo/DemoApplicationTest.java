package com.joshlong.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;

@Log4j2
class DemoApplicationTest {

	@Test
	public void go() {
		SpringApplication.run(DemoApplication.class);
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
