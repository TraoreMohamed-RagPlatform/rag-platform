package com.timz.rag_platform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private RecaptchaService recaptchaService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var encoder = passwordEncoder();
        var admin = User.withUsername("admin@timz.com")
            .password(encoder.encode("admin123"))
            .roles("ADMIN")
            .build();
        var user = User.withUsername("user@timz.com")
            .password(encoder.encode("user123"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(admin, user) {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                if (loginAttemptService.isBlocked(username)) {
                    throw new UsernameNotFoundException("Compte bloque apres trop de tentatives.");
                }
                return super.loadUserByUsername(username);
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");

            if (loginAttemptService.getAttempts(username) >= 5) {
                String captchaToken = request.getParameter("g-recaptcha-response");
                if (captchaToken == null || captchaToken.isBlank() || !recaptchaService.validerToken(captchaToken)) {
                    response.sendRedirect("/login?captcha_required");
                    return;
                } else {
                    loginAttemptService.resetAfterCaptcha(username);
                }
            }

            loginAttemptService.loginFailed(username);
            int remaining = loginAttemptService.getRemainingAttempts(username);

            if (remaining == 0) {
                response.sendRedirect("/login?captcha_required");
            } else {
                response.sendRedirect("/login?error&remaining=" + remaining);
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            loginAttemptService.loginSucceeded(authentication.getName());
            response.sendRedirect("/dashboard");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login*").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler())
                .failureHandler(failureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/logout",
                    "/api/chat",
                    "/documents/upload",
                    "/documents/supprimer/**",
                    "/documents/modifier/**",
                    "/documents/supprimer-multiple",
                    "/admin/ajouter",
                    "/admin/supprimer/**"
                )
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' cdnjs.cloudflare.com www.google.com www.gstatic.com; style-src 'self' 'unsafe-inline' cdnjs.cloudflare.com fonts.googleapis.com; font-src 'self' cdnjs.cloudflare.com fonts.gstatic.com; img-src 'self' data: www.gstatic.com; frame-src www.google.com;")
                )
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            );
        return http.build();
    }
}