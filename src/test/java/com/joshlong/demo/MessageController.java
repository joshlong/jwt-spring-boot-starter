package com.joshlong.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
class MessageController {

	@GetMapping("/greetings")
	Map<String, String> greet(Principal principal) {
		return singletonMap("greeting", "Hello " + principal.getName() + "!");
	}

}
