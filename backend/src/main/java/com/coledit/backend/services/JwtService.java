package com.coledit.backend.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service class for handling JWT (JSON Web Token) operations.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    public String secretKey;

    @Value("${security.jwt.expire-time}")
    public long jwtExpiration;

    /**
     * Extracts the username from the JWT token.
     * 
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a claim from the JWT token using a resolver function.
     * 
     * @param token          the JWT token
     * @param claimsResolver the function to resolve the claim
     * @return the resolved claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            return null;
        } catch (Exception e) {
            System.out.println("Exception in extractAllClaims: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates a JWT token for a UserDetails object.
     * 
     * @param userDetails the UserDetails object
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return generateToken(claims, userDetails);
    }

    /**
     * Generates a JWT token with extra claims for a UserDetails object.
     * 
     * @param extraClaims additional claims to include in the token
     * @param userDetails the UserDetails object
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Gets the expiration time of the JWT tokens.
     * 
     * @return the expiration time in milliseconds
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Builds a JWT token with specified claims and expiration time.
     * 
     * @param extraClaims additional claims to include in the token
     * @param userDetails the UserDetails object
     * @param expiration  the expiration time in milliseconds
     * @return the built JWT token
     */
    public String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if a JWT token is valid for a given UserDetails object.
     * 
     * @param token       the JWT token
     * @param userDetails the UserDetails object
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if a JWT token is expired.
     * 
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            boolean isOrNot = extractExpiration(token).before(new Date());
            return isOrNot;
        } catch (Exception e) {
            return true; // Handle exceptions if token parsing fails
        }
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date extracted from the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .setAllowedClockSkewSeconds(3600)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts roles (authorities) from a JWT token.
     * 
     * @param token the JWT token
     * @return the roles extracted from the token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    /**
     * Retrieves the signing key used for JWT verification.
     * 
     * @return the signing key
     */
    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts a JWT token from a HttpServletRequest.
     * 
     * @param request the HttpServletRequest containing cookies
     * @return the JWT token extracted from the request's cookies
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
