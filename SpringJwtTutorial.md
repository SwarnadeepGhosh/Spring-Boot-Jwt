# Spring Boot JWT Tutorial



### Setup

```
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-web
h2 database
lombok
```



***application.properties***

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.initialization-mode=always
jwt.secret=secretkey
jwt.token.validity=900000
```



## SpringBoot JPA Authentication

1. **CustomUser.java** - User Model 
2. **UserRepo.java** - JPA Repository used to fetch data from DB
3. **CustomUserDetailService.java** - implementation of UserDetailService which will override loadUserByUsername() method
4. **SecurityConfig.java** - Main config file for Spring Security
5. **WelcomeController.java** - Controller
6. **SpringJwtAppApplication.java** - Main class



**CustomUser.java** - User Model 

```java
package com.swarna.boot.SpringJwtApp.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CustomUser {

	@Id
	private int id;
	private String username;
	private String password;
	private String email;
}
```



**UserRepo.java** - JPA Repository used to fetch data from DB

```java
package com.swarna.boot.SpringJwtApp.repo;
public interface UserRepo extends JpaRepository<CustomUser, Integer> {
	CustomUser findByUsername(String username);
}
```



### UserDetailsServiceImpl

**CustomUserDetailService.java** - implementation of UserDetailService which will override loadUserByUsername() method

```java
package com.swarna.boot.SpringJwtApp.service;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.swarna.boot.SpringJwtApp.model.CustomUser;
import com.swarna.boot.SpringJwtApp.repo.UserRepo;

@Service
public class CustomUserDetailService implements UserDetailsService {
	@Autowired
	private UserRepo userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		CustomUser user = userRepo.findByUsername(username);
		return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
	}
}
```



**SecurityConfig.java** - Main config file for Spring Security

```java
package com.swarna.boot.SpringJwtApp.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.swarna.boot.SpringJwtApp.service.CustomUserDetailService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CustomUserDetailService customUserDetailService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailService);
	}
	
    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
}
```



**WelcomeController.java** - Controller

```java
package com.swarna.boot.SpringJwtApp.controller;
@RestController
public class WelcomeController {

	@GetMapping("/")
	public String welcome() {
		return "You are Authenticated !!!";
	}
}
```



**SpringJwtAppApplication.java** - Main class

```java
package com.swarna.boot.SpringJwtApp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.swarna.boot.SpringJwtApp.model.CustomUser;
import com.swarna.boot.SpringJwtApp.repo.UserRepo;

@SpringBootApplication
public class SpringJwtAppApplication {
	
	@Autowired
	private UserRepo userRepo;
	
    @PostConstruct // This annotaion will make below method as init, so that it initialize on application startup
    public void initUsers() {
        List<CustomUser> users = Stream.of(
                new CustomUser(101, "javatechie", "password", "javatechie@gmail.com"),
                new CustomUser(102, "user1", "pwd1", "user1@gmail.com"),
                new CustomUser(103, "user2", "pwd2", "user2@gmail.com"),
                new CustomUser(104, "user3", "pwd3", "user3@gmail.com")
        ).collect(Collectors.toList());
        userRepo.saveAll(users);
    }

	public static void main(String[] args) {
		SpringApplication.run(SpringJwtAppApplication.class, args);
	}
}
```





## Configuring JWT

***pom.xml***

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```



We need to add below files : 

1. **AuthRequest.java** - Model class for Authentication Request
2. **JwtUtil.java** - Used for generate and Validate Token 
3. **JwtFilter.java** (extends OncePerRequestFilter) - Authorize any request using JWT string - JwtUtil.validateToken() method

We need to edit below files : 

1. **SecurityConfig.java** - Adding authenticationManager() and configure(HttpSecurity http) method.
2. **WelcomeController.java** - `@PostMapping("/authenticate")` will be added, which will generate token



### Steps to follow

**JwtUtil Methods working**

1. `generateToken()` -> based on username and we are giving one empty map (claims) -> Go to createToken

2.  `createToken()` -> setting issued date, expiry of that token and signWith as a secret password with algorithm `HS256` -> returns a JWT String

3. `validateToken()` -> sending token(username will be extracted) and `UserDetails` (which is nothing but Username & password) -> Returns Boolean if token username matches with existing user details and if token is not expired

We will use only generateToken() and validateToken() method in our controller.



**SecurityConfig (extends WebSecurityConfigurerAdapter) methods addition**

1. We have to create a bean name `AUTHENTICATION_MANAGER`, which will be used in controller to authenticate

2. Adding a endpoint `@PostMapping("/authenticate")` in Controller, which will authenticate username and password using `AuthenticationManager.authenticate()` method. If authentication success, then it will return a jwt string using `jwtUtil.generateToken(authRequest.getUsername()`

3. Now we need to disable security for /authenticate endpoint, and authenticate all other endspoints in `SecurityConfig` class



**Authorizing other request through Bearer**

Creating Filter, which will take the JWT from bearer Auth header, and validate that for all requests. If validated, then setting the auth token in `SecurityContextHolder` , 



### Final Codes

**AuthRequest.java** - Model class for Authentication Request

```java
package com.swarna.boot.SpringJwtApp.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

	private String username;
	private String password;
}
```



### JwtUtil

**JwtUtil.java** - Used for generate and Validate Token 

```java
package com.swarna.boot.SpringJwtApp.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Used for generate and Validate Token
@Service
public class JwtUtil {
	
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.token.validity}")
	private int tokenValidity;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public String generateToken(String username) {
//		System.out.println(secret);
//		System.out.println(tokenValidity);
		
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, username);
	}

	private String createToken(Map<String, Object> claims, String subject) {
		// Expiration : 1000 * 60 * 60 * 10
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenValidity))
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}
```



### JwtFilter

**JwtFilter.java** (extends OncePerRequestFilter) - Authorize any request using JWT string - JwtUtil.validateToken() method

```java
package com.swarna.boot.SpringJwtApp.filter;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.swarna.boot.SpringJwtApp.service.CustomUserDetailService;
import com.swarna.boot.SpringJwtApp.util.JwtUtil;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private CustomUserDetailService service;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		String token = null;
		String username = null;

		// Extracting Token from header and extracting Username from token
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7); // Bearer eyJhbGc...
			username = jwtUtil.extractUsername(token);
		}

		// getting userDetails from CustomUserDetailService and validating token
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = service.loadUserByUsername(username);

			if (jwtUtil.validateToken(token, userDetails)) {
				UsernamePasswordAuthenticationToken userPassAuthToken = 
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
				userPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(userPassAuthToken);
			}
		}
		
		filterChain.doFilter(request, response);
	}
}
```



### WebSecurityConfigurerAdapter

**SecurityConfig.java** - Adding authenticationManager() and configure(HttpSecurity http) method.

```java
package com.swarna.boot.SpringJwtApp.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.swarna.boot.SpringJwtApp.filter.JwtFilter;
import com.swarna.boot.SpringJwtApp.service.CustomUserDetailService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CustomUserDetailService customUserDetailService;
	@Autowired
	private JwtFilter jwtFilter;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailService);
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Allowing only /authenticate and authenticate any other endpoints
		http.csrf().disable().authorizeRequests().antMatchers("/authenticate").permitAll().anyRequest().authenticated()
		.and().exceptionHandling().and()
		// setting session policy as stateless to use filter for Authorization using JWT
		// stateless means input are not stored in servermemory and cookies
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		// setting filter
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
```



### Controller

**WelcomeController.java** - `@PostMapping("/authenticate")` will be added, which will generate token

```java
package com.swarna.boot.SpringJwtApp.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.swarna.boot.SpringJwtApp.model.AuthRequest;
import com.swarna.boot.SpringJwtApp.util.JwtUtil;

@RestController
public class WelcomeController {

	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private AuthenticationManager authenticationManager;

	@GetMapping("/")
	public String welcome() {
		return "You are Authenticated !!!";
	}

	@PostMapping("/authenticate")
	public String generateToken(@RequestBody AuthRequest authRequest) throws Exception {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

		} catch (Exception e) {
			throw new Exception("Invalid Username / password");
		}

		return jwtUtil.generateToken(authRequest.getUsername());
	}
}
```





## Testing API

```sh
curl --location --request POST 'http://localhost:8080/authenticate' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username" : "user1",
    "password" : "pwd1"
}'

# Output
# eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImV4cCI6MTY1NTgzODAwNSwiaWF0IjoxNjU1ODM3MTA1fQ.Kzu6-2Z7gCJZqN_edehU0TTn0B6cRDYh-NjEYpnh0Xk
```

```sh
curl --location --request GET 'http://localhost:8080' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImV4cCI6MTY1NTgzODAwNSwiaWF0IjoxNjU1ODM3MTA1fQ.Kzu6-2Z7gCJZqN_edehU0TTn0B6cRDYh-NjEYpnh0Xk'

# Output
# You are Authenticated !!!
```



