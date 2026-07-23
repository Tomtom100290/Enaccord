package com.enaccord.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Autoriser l'accès public à l'accueil, l'inscription, la connexion et aux fichiers statiques (CSS/JS)
                        .requestMatchers("/", "/inscription", "/connexion", "/css/**", "/js/**", "/images/**").permitAll()
                        // Toutes les autres pages (ex: /chansons/nouvelle, /chansons/modifier) demandent une connexion
                        .requestMatchers("/groupes/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/connexion") // Notre formulaire personnalisé
                        .usernameParameter("email") // Utiliser l'email au lieu du "username" standard
                        .defaultSuccessUrl("/", true) // Redirection après connexion réussie
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/deconnexion")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    // Le bean magique pour crypter les mots de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}