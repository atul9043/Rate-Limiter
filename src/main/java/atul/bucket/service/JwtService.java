package atul.bucket.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String username){

        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
            .claims().add(claims)
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis()+1000*60*60*24))
            .and()
            .signWith(getKey())
            .compact();
    }

    public Key getKey(){
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    public String extractusername(String token){

        Claims claims = Jwts.parser()
                            .verifyWith((SecretKey) getKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails){

        String username = extractusername(token);
        return username.equals(userDetails.getUsername())&&!isTokenExpired(token);
    }

    public boolean isTokenExpired(String token){
        Claims claims = Jwts.parser()
                            .verifyWith((SecretKey) getKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return claims.getExpiration().before(new Date());
    }




}
