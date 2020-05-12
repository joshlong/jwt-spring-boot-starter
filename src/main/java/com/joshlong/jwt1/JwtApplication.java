package com.joshlong.jwt1;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * @author Rob Winch
 * @author Josh Long
 */
@SpringBootApplication
public class JwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtApplication.class, args);
    }
}


@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests(ae -> ae.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    }

    @Bean
    InMemoryUserDetailsManager userDetailsManager() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    JwtDecoder decoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }
}


@Configuration
class NimbusConfig {

    @Bean
    RSAKey rsaKey() throws Exception {
        return new RSAKeyGenerator(2048)
                .keyID("123")
                .generate();
    }

    @Bean
    RSASSASigner rsassaSigner(RSAKey rsaKey) throws JOSEException {
        return new RSASSASigner(rsaKey);
    }
}


@RestController
class MessageController {

    @GetMapping("/")
    public String message(Principal principal) {
        return "Hello " + principal.getName() + "!";
    }
}


@RestController
class JwtController {

    private final JWSSigner signer;

    JwtController(JWSSigner signer) {
        this.signer = signer;
    }

    @PostMapping("/token")
    String token(Principal principal) throws Exception {
        var now = Instant.now();
        var claims = new JWTClaimsSet.Builder()
                .issuer("http://localhost:8080")
                .audience("http://localhost:8080")
                .expirationTime(Date.from(now.plus(Duration.ofDays(2))))
                .issueTime(Date.from(now))
                .subject(principal.getName())
                .build();
        var jwsObject = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                new Payload(claims.toJSONObject())
        );
        jwsObject.sign(this.signer);
        return jwsObject.serialize();

    }
}

class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {

    @Override
    public void init(HttpSecurity builder) throws Exception {
        builder.csrf(AbstractHttpConfigurer::disable);
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {

    }

    public static MyCustomDsl customDsl() {
        return new MyCustomDsl();
    }
}