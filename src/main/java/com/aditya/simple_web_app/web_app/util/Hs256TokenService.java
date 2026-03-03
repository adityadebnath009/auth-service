package com.aditya.simple_web_app.web_app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static java.security.KeyRep.Type.SECRET;

@Component
@Primary
public class Hs256TokenService implements TokenService {

    private final Key accessSigningKey;
    private final Key refreshSigningKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;


    public Hs256TokenService( @Value("${jwt.access.secret}") String accessSecret,
                              @Value("${jwt.refresh.secret}") String refreshSecret,
                              @Value("${jwt.access.expiration-ms}") long accessExpirationMs,
                              @Value("${jwt.refresh.expiration-ms}") long refreshExpirationMs)
    {

        this.accessSigningKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(accessSecret)
        );
        this.refreshSigningKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(refreshSecret)
        );
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;

    }

    public String generateAccessToken(UserDetails userDetails) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("type", "access")// Definning the token type
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(accessSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateRefreshToken(UserDetails userDetails) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("type", "refresh")// Definning the token type
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(refreshSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }



    public String extractUsernameFromRefreshToken(String token) {
        return extractClaims(token, refreshSigningKey).getSubject();
    }
    public String extractUserNameFromAccessToken(String token) {
        return extractClaims(token, accessSigningKey).getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {

        Claims claims = extractClaims(token, accessSigningKey);

        return claims.getSubject().equals(userDetails.getUsername())
                && "access".equals(claims.get("type"))
                && !claims.getExpiration().before(new Date());
    }

    @Override
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {

        Claims claims = extractClaims(token, refreshSigningKey);

        return claims.getSubject().equals(userDetails.getUsername())
                && "refresh".equals(claims.get("type"))
                && !claims.getExpiration().before(new Date());
    }



    private Claims extractClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
