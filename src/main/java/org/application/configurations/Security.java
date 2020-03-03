package org.application.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

    final
    DataSource dataSource;

    public Security(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setUsersByUsernameQuery("SELECT username, password, enabled FROM APP_USER WHERE username=?;");
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery("SELECT username, authority FROM APP_USER WHERE username=?;");
        jdbcUserDetailsManager.setDataSource(dataSource);
        return jdbcUserDetailsManager;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/profile/*").hasAnyAuthority("ROLE_TRAINER", "ROLE_ADMIN", "ROLE_USER")
                .antMatchers("/**").permitAll()
                .and()
                .formLogin()
                .defaultSuccessUrl("/profile/primary")
                .and()
                .logout()
                .logoutSuccessUrl("/");

        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
    }
}
