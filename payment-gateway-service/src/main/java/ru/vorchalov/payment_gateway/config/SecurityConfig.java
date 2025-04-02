package ru.vorchalov.payment_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.vorchalov.payment_gateway.security.ApiKeyAuthenticationFilter;
import ru.vorchalov.payment_gateway.security.RestAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyFilter;

    private final RestAuthenticationSuccessHandler successHandler;

    public SecurityConfig(ApiKeyAuthenticationFilter apiKeyFilter,
                          RestAuthenticationSuccessHandler successHandler) {
        this.apiKeyFilter = apiKeyFilter;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/auth/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cabinet/**").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form
                        .successHandler(successHandler)
                        .permitAll()
                )
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService uds) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
