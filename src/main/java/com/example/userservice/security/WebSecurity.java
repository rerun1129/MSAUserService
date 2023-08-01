package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {
    private final UserService userService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final BCryptPasswordEncoder encoder;
    private final Environment environment;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(userService, environment);
        authenticationFilter.setAuthenticationManager(authenticationManager());
        http.csrf().disable();
        http.headers(authorize -> authorize
                .frameOptions().disable())
                .authorizeHttpRequests (authorize ->
                                                authorize.antMatchers ( "/**" )
                                                            .permitAll ( )
                                                            .and ( )
                                                            .addFilter ( authenticationFilter ) );
        return http.build ( );
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception{
        return this.authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    AuthenticationManager authenticationManager ( AuthenticationManagerBuilder builder ) throws Exception {
        return builder.userDetailsService(userService).passwordEncoder(encoder).and().build();
    }
}
