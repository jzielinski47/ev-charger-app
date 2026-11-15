package jz.pk.evcm.security;

import jz.pk.evcm.config.RateLimitProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private static final RateLimitProperties PROPERTIES =
            new RateLimitProperties(true, 1, 20, Duration.ofMinutes(1), Duration.ofMinutes(2));

    private final RateLimitFilter filter = new RateLimitFilter(PROPERTIES, JsonMapper.builder().build());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static MockHttpServletRequest requestFrom(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");
        request.setRemoteAddr(ip);
        return request;
    }

    private static void authenticateAs(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        User principal = new User(email, "", authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    private MockHttpServletResponse send(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void anonymous_secondRequestWithinPeriod_isRejected() throws Exception {
        MockHttpServletResponse first = send(requestFrom("10.0.0.1"));
        MockHttpServletResponse second = send(requestFrom("10.0.0.1"));

        assertThat(first.getStatus()).isEqualTo(200);
        assertThat(first.getHeader(RateLimitFilter.REMAINING_HEADER)).isEqualTo("0");
        assertThat(second.getStatus()).isEqualTo(429);
        assertThat(Long.parseLong(second.getHeader(HttpHeaders.RETRY_AFTER))).isBetween(1L, 60L);
        assertThat(second.getContentAsString()).contains("Too Many Requests");
    }

    @Test
    void anonymous_differentIps_haveSeparateBuckets() throws Exception {
        send(requestFrom("10.0.0.1"));

        assertThat(send(requestFrom("10.0.0.2")).getStatus()).isEqualTo(200);
    }

    @Test
    void authenticated_twentyFirstRequest_isRejected() throws Exception {
        authenticateAs("user@test.dev", "ROLE_USER");

        for (int i = 0; i < 20; i++) {
            assertThat(send(requestFrom("10.0.0.1")).getStatus()).isEqualTo(200);
        }

        assertThat(send(requestFrom("10.0.0.1")).getStatus()).isEqualTo(429);
    }

    @Test
    void authenticated_isKeyedByEmailNotIp() throws Exception {
        authenticateAs("user@test.dev", "ROLE_USER");
        for (int i = 0; i < 20; i++) {
            send(requestFrom("10.0.0." + i));
        }

        assertThat(send(requestFrom("10.0.0.99")).getStatus()).isEqualTo(429);

        authenticateAs("other@test.dev", "ROLE_USER");
        assertThat(send(requestFrom("10.0.0.99")).getStatus()).isEqualTo(200);
    }

    @Test
    void admin_isNeverLimited() throws Exception {
        authenticateAs("admin@test.dev", "ROLE_USER", "ROLE_ADMIN");

        for (int i = 0; i < 50; i++) {
            MockHttpServletResponse response = send(requestFrom("10.0.0.1"));
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getHeader(RateLimitFilter.REMAINING_HEADER)).isNull();
        }
    }

    @Test
    void exemptPaths_areNotLimited() throws Exception {
        for (String path : List.of("/api/auth/signup", "/api/auth/login", "/api/catalog/brands", "/api/catalog/brands/tesla/models", "/swagger-ui/index.html", "/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/swagger-config")) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            request.setRemoteAddr("10.0.0.1");
            assertThat(send(request).getStatus()).isEqualTo(200);
        }

        assertThat(send(requestFrom("10.0.0.1")).getStatus()).isEqualTo(200);
    }
}
