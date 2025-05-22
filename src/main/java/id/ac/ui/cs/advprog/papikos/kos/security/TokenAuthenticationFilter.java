package id.ac.ui.cs.advprog.papikos.kos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final RestTemplate restTemplate;
    private final String authVerifyUrl;

    public TokenAuthenticationFilter(RestTemplate restTemplate, @Value("${auth.server.verify.url}") String authVerifyUrl) {
        this.restTemplate = restTemplate;
        this.authVerifyUrl = authVerifyUrl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token); // Set Bearer token
            HttpEntity<Void> entity = new HttpEntity<>(headers); // No body

            try {
                logger.debug("Verifying token with auth server at URL: {}", authVerifyUrl);
                // Make POST request to auth server
                ResponseEntity<String> verificationResponse = restTemplate.exchange(
                        authVerifyUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (verificationResponse.getStatusCode().is2xxSuccessful()) {
                    logger.info("Token verified successfully for request URI: {}", request.getRequestURI());
                    // Token is valid, set up Spring Security context
                    // You might want to parse verificationResponse.getBody() if it contains user details/roles
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            "authenticatedUser", // Principal (e.g., user ID from token or response)
                            null,                // Credentials
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("Token verification failed with status: {}. Response: {}", verificationResponse.getStatusCode(), verificationResponse.getBody());
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Authentication Failed: Token verification unsuccessful");
                    return; // Stop filter chain
                }
            } catch (HttpClientErrorException e) {
                logger.warn("Client error during token verification: Status {}, Body {}", e.getStatusCode(), e.getResponseBodyAsString());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // It's good practice to not expose too much detail from the auth server's error to the client
                response.getWriter().write("Authentication Failed: Invalid token or authentication service error.");
                return; // Stop filter chain
            } catch (RestClientException e) {
                logger.error("Error connecting to authentication service: {}", e.getMessage(), e);
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Authentication Failed: Could not connect to authentication service.");
                return; // Stop filter chain
            }
        } else {
            logger.debug("No Bearer token found in Authorization header for request URI: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response); // Continue filter chain
    }
}