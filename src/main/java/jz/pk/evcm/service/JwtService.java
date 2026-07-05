/*
* following service was implemented with docs ref. https://github.com/jwtk/jjwt
* */


package jz.pk.evcm.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;



    @Value("${security.jwt.expiration-time}")
    private long jwtExpirationMs;

   public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
       final Claims claims = extractAllClaims(token);
       return claimsResolver.apply(claims);
   }

   public String generateToken(String email) {
       return Jwts.builder()
               .subject(email)
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
               .signWith(getSignInKey())
               .compact();
   }

    public String extractEmail(String token) {
        return null;
    }

    public String extractRole(String token) {
        return null;
    }

    public Long getExpirationTime(String token) {
        return null;
    }

   private Claims extractAllClaims(String token) {

   }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}