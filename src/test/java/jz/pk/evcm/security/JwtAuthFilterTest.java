package jz.pk.evcm.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String TOKEN = "token";

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthFilter filter;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    static Stream<JwtException> invalidTokenFailures() {
        return Stream.of(
                new ExpiredJwtException(null, null, "expired"),
                new MalformedJwtException("malformed"),
                new SignatureException("bad signature"));
    }

    @ParameterizedTest
    @MethodSource("invalidTokenFailures")
    void invalidToken_continuesChainAsAnonymous(JwtException failure) throws Exception {
        when(jwtService.isTokenValid(TOKEN)).thenThrow(failure);

        filter.doFilter(requestWithBearer(TOKEN), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validToken_setsPrincipalWithEmail() throws Exception {
        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(TOKEN)).thenReturn("user@test.dev");
        when(jwtService.extractAuthorities(TOKEN)).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        filter.doFilter(requestWithBearer(TOKEN), response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(chain.getRequest()).isNotNull();
        assertThat(authentication).isNotNull();
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo("user@test.dev");
    }

    @Test
    void missingHeader_continuesChainAsAnonymous() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
