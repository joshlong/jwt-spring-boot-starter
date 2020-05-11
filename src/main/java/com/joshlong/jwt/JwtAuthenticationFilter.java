package com.joshlong.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/*
* Taken from <a href="https://grobmeier.solutions/spring-security-5-jwt-basic-auth.html">this interesting article</a>.
*/
@Log4j2
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final String jwtAudience;

	private final String jwtIssuer;

	private final String jwtSecret;

	private final String jwtType;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager, String jwtAudience, String jwtIssuer,
			String jwtSecret, String jwtType, String loginUrl) {
		this.jwtAudience = jwtAudience;
		this.jwtIssuer = jwtIssuer;
		this.jwtSecret = jwtSecret;
		this.jwtType = jwtType;
		this.setAuthenticationManager(authenticationManager);
		setFilterProcessesUrl(loginUrl);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain, Authentication authentication) {
		var user = (User) authentication.getPrincipal();
		var secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
		var token = Jwts.builder().signWith(secretKey, SignatureAlgorithm.HS512).setHeaderParam("typ", jwtType)
				.setIssuer(jwtIssuer).setAudience(jwtAudience).setSubject(user.getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + 864000000)).compact();
		response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
	}

}
