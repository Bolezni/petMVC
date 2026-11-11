package com.example.demo.api.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-ttl-ms}")
    private long jwtExpiration;

    @Value("${jwt.refresh-ttl-ms}")
    private long refreshExpiration;


    private SecretKey getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getAllClaims(String token){
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception ex){
            throw new JwtException("Invalid JWT token: " + ex.getMessage());
        }
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolve){
        Claims claims = getAllClaims(token);
        return claimsResolve.apply(claims);
    }

    private Date getExpirationDate(String token){
        return getClaim(token, Claims::getExpiration);
    }

    private boolean validateToken(String token){
        return !getExpirationDate(token).before(new Date());
    }

    public String usernameFromToken(String token){
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValidate(String token, UserDetails userDetails){
        boolean isValid = validateToken(token);
        String username = usernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && isValid);
    }

    private String buildToken(UserDetails userDetails, long expirationTime){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey())
                .compact();
    }

    public String buildJwtToken(UserDetails userDetails){
        return buildToken(userDetails, jwtExpiration);
    }

    public String buildRefreshToken(UserDetails userDetails){
        return buildToken(userDetails, refreshExpiration);
    }
}
