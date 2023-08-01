package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final UserService userService;
    private final Environment environment;

    @Override
    public Authentication attemptAuthentication (
            HttpServletRequest request,
            HttpServletResponse response ) throws AuthenticationException {
        try {
            RequestLogin creds = new ObjectMapper ().readValue ( request.getInputStream (), RequestLogin.class );
            return getAuthenticationManager ()
                    .authenticate ( new UsernamePasswordAuthenticationToken (
                                            creds.getEmail (),
                                            creds.getPassword (),
                                            new ArrayList<> () ) );
        } catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }

    @Override
    protected void successfulAuthentication (
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult ) throws IOException, ServletException {
        String username = ( ( User ) authResult.getPrincipal ( ) ).getUsername ( );
        UserDto userDto = userService.getUserDetailsByEmail ( username );

        String token = Jwts
                .builder ( )
                .setSubject ( userDto.getUserId () )
                .setExpiration ( new Date (System.currentTimeMillis () +
                                Long.parseLong (environment.getProperty ( "token.expiration_time" ))) )
                .signWith ( SignatureAlgorithm.HS512, environment.getProperty ( "token.secret" ) )
                .compact ( );
        response.addHeader ( "token", token );
        response.addHeader ( "userId", userDto.getUserId () );

    }
}
