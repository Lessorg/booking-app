package test.project.bookingapp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilsTests {
    private JwtUtils jwtUtils;
    private String validToken;
    private String invalidToken;
    private final String secret = "testsecretkeyforjwttokengenerationtestsecretkeyforjwttoken";

    @BeforeEach
    void setUp() {
        long expiration = 60000L;
        jwtUtils = new JwtUtils(secret, expiration);
        validToken = jwtUtils.generateToken("testuser");
        invalidToken = "invalid.token.here";
    }

    @Test
    @DisplayName("Generate token should return a non-null token")
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtUtils.generateToken("testuser");
        assertNotNull(token);
    }

    @Test
    @DisplayName("Validate token should return true for a valid token")
    void isValidToken_ValidToken_ReturnsTrue() {
        assertTrue(jwtUtils.isValidToken(validToken));
    }

    @Test
    @DisplayName("Validate token should throw JwtException for an invalid token")
    void isValidToken_InvalidToken_ThrowsJwtException() {
        JwtException exception = assertThrows(JwtException.class,
                () -> jwtUtils.isValidToken(invalidToken));
        assertEquals("Expired or invalid JWT token", exception.getMessage());
    }

    @Test
    @DisplayName("Get username should return the correct username from the token")
    void getUsername_ValidToken_ReturnsUsername() {
        assertEquals("testuser", jwtUtils.getUsername(validToken));
    }

    @Test
    @DisplayName("Get username should throw JwtException for an invalid token")
    void getUsername_InvalidToken_ThrowsJwtException() {
        assertThrows(JwtException.class,
                () -> jwtUtils.getUsername(invalidToken));
    }

    @Test
    @DisplayName("Validate token should return false for an expired token")
    void isValidToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        long shortExpiration = 1L;
        JwtUtils shortLivedJwtService =
                new JwtUtils(secret, shortExpiration);
        String expiredToken = shortLivedJwtService.generateToken("testuser");
        Thread.sleep(2);
        assertThrows(JwtException.class, () -> shortLivedJwtService.isValidToken(expiredToken));
    }
}
