package com.coledit.backend.configs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * SecurityConfiguration class configures the security settings for the
 * application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Value("${custom.hostname}")
    private String hostname;

    /**
     * Constructor to initialize the SecurityConfiguration with necessary
     * dependencies.
     *
     * @param jwtAuthenticationFilter the custom JWT authentication filter
     * @param authenticationProvider  the custom authentication provider
     */
    public SecurityConfiguration(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthorizationFilter jwtAuthorizationFilter,
            AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disables CSRF protection
                .authorizeHttpRequests(auth -> auth
                        // Developer setup
                        .requestMatchers("/**").permitAll() // Allows requests to /auth/** without authentication
                        .anyRequest().authenticated() // Requires authentication for all other requests
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Configures session management to be
                // stateless
                )
                .authenticationProvider(authenticationProvider) // Sets the custom authentication provider
                // Adds the JWT authentication filter before the username-password
                // authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT
                                                                                                      // Authentication
                                                                                                      // Filter
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(c -> c.configurationSource(corsConfigurationSource())) // Configures CORS
                .requiresChannel(channel -> channel
                        .anyRequest()); // Requires HTTPS for all requests

        return http.build();
    }

    /**
     * Configures CORS settings for the application.
     *
     * @return the configured CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Sets the allowed origins for CORS
        configuration.setAllowedOrigins(List.of(
                "https://" + hostname));
        // Sets the allowed HTTP methods for CORS
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS", "DELETE"));
        // Allows all headers for CORS
        configuration.setAllowedHeaders(List.of("*"));
        // Allows credentials for CORS
        configuration.setAllowCredentials(true);
        // Sets the max age for CORS pre-flight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Registers the CORS configuration for all paths
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
