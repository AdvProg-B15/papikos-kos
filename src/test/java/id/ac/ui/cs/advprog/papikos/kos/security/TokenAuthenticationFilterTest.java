package id.ac.ui.cs.advprog.papikos.kos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.kos.dto.VerifyTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private final String AUTH_VERIFY_URL = "http://localhost:8080/auth";
    private final String INTERNAL_TOKEN_SECRET = "test-secret";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenAuthenticationFilter, "authVerifyUrl", AUTH_VERIFY_URL);
        ReflectionTestUtils.setField(tokenAuthenticationFilter, "internalTokenSecret", INTERNAL_TOKEN_SECRET);
        SecurityContextHolder.clearContext(); // Ensure clean context for each test
    }

    @Test
    void doFilterInternal_noToken_continuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Internal-Token")).thenReturn(null);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validBearerToken_authenticatesUser() throws ServletException, IOException {
        String token = "valid-token";
        String userId = "user-123";
        String userRole = "PEMILIK";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getHeader("X-Internal-Token")).thenReturn(null);

        VerifyTokenResponse.Data tokenData = VerifyTokenResponse.Data.builder().userId(userId).role(userRole).build();
        VerifyTokenResponse verifyResponse = VerifyTokenResponse.builder().data(tokenData).build();
        String verifyResponseJson = "{\"data\":{\"userId\":\"" + userId + "\",\"role\":\"" + userRole + "\"}}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(verifyResponseJson, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(AUTH_VERIFY_URL + "/api/v1/verify"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(verifyResponseJson, VerifyTokenResponse.class)).thenReturn(verifyResponse);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userId, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(userRole)));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidBearerToken_verificationFails_returnsUnauthorized() throws ServletException, IOException {
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getHeader("X-Internal-Token")).thenReturn(null);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);
        // objectMapper.readValue will be called by the filter, need to mock its behavior for non-2xx status
        // For this specific path, if status is not 2xx, objectMapper.readValue might not be reached for VerifyTokenResponse.class
        // as the filter returns early.

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Authentication Failed: Token verification unsuccessful"));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_authServiceHttpClientError_returnsUnauthorized() throws ServletException, IOException {
        String token = "token-causes-client-error";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getHeader("X-Internal-Token")).thenReturn(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden from auth service"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Authentication Failed: Invalid token or authentication service error."));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_authServiceRestClientException_returnsInternalServerError() throws ServletException, IOException {
        String token = "token-causes-rest-error";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getHeader("X-Internal-Token")).thenReturn(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertTrue(stringWriter.toString().contains("Authentication Failed: Could not connect to authentication service."));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validInternalToken_authenticatesAsInternalService() throws ServletException, IOException {
        when(request.getHeader("X-Internal-Token")).thenReturn(INTERNAL_TOKEN_SECRET);
        // No Authorization header needed if X-Internal-Token is present and valid

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("internal-service", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("INTERNAL")));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidInternalToken_returnsUnauthorized() throws ServletException, IOException {
        when(request.getHeader("X-Internal-Token")).thenReturn("wrong-secret");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Authentication Failed: Invalid internal token."));
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

     @Test
    void doFilterInternal_bearerTokenPresentButNotStartingWithBearer_continuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("NotBearer token");
        when(request.getHeader("X-Internal-Token")).thenReturn(null);

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

