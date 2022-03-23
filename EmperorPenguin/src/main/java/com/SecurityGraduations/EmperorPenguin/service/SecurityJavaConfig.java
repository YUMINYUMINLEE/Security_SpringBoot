package com.SecurityGraduations.EmperorPenguin.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityJavaConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeRequests()
                .mvcMatchers("/","/css/**")
                .permitAll();
        httpSecurity
                // cors 방지
                .cors().disable()
                // csrf 방지
                .csrf().disable()
                // 기존 로그인 페이지 삭제
                .formLogin().disable()
                .headers().frameOptions().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BruteForce attack을 막기 위해 고안된 방식
        // return new Pbkdf2PasswordEncoder();
        // return new Argon2PasswordEncoder();
        return new BCryptPasswordEncoder();
    }
}