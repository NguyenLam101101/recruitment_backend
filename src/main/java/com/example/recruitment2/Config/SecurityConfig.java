package com.example.recruitment2.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    final String webServer = new Link().getWebServer();
    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
                    .requestMatchers("/**").permitAll();
//                    .and()
//                .formLogin()
//                    .loginPage(webServer+"/login")
//                    .loginProcessingUrl("/login")
//                    .defaultSuccessUrl("/default-success-login")
//                    .failureUrl(webServer+"/login?error=true")
//                    .usernameParameter("email")
//                    .passwordParameter("password")
//                    .permitAll()
//                    .and()
//                .logout()
//                    .logoutUrl("/logout")
//                    .logoutSuccessUrl(webServer+"/login")
//                    .invalidateHttpSession(true)
//                    .deleteCookies("JSESSIONID");
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
