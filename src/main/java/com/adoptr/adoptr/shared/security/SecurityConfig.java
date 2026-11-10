package com.adoptr.adoptr.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtAuthFilter jwtFilter;

    public SecurityConfig(JwtAuthFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity){
        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> {
                    request.requestMatchers("/auth/**").permitAll();
                    request.requestMatchers(SWAGGER_WHITELIST).permitAll();
                    // Catálogo oficial de ubicaciones: sin datos personales, lo usan visitantes anónimos.
                    request.requestMatchers(HttpMethod.GET, "/provinces", "/provinces/**").permitAll();
                    // Va antes que /adoptions/{id}: si no, "mine" se tomaría como un id público.
                    request.requestMatchers(HttpMethod.GET, "/adoptions/mine").authenticated();
                    // Catálogo y detalle públicos; con un JWT válido el filtro igual carga el usuario (viewerIsOwner).
                    request.requestMatchers(HttpMethod.GET, "/adoptions", "/adoptions/{id}").permitAll();
                    // Perfiles ajenos y sus publicaciones, públicos. El perfil propio (/profile) sigue con token.
                    request.requestMatchers(HttpMethod.GET, "/profiles/{id}", "/profiles/{id}/adoptions").permitAll();
                    // Imágenes en disco de desarrollo, públicas como las del bucket. Con S3 la ruta responde 404.
                    request.requestMatchers(HttpMethod.GET, "/local-images/**").permitAll();
                    request.anyRequest().authenticated();
                })
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(ApiErrorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(ApiErrorHandlers.accessDeniedHandler()))
                .oauth2Login(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
