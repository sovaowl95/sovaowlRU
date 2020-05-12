package ru.sovaowltv.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import ru.sovaowltv.config.security.MyUsernamePasswordAuthenticationFilter;
import ru.sovaowltv.config.security.UserDetailsServiceImpl;
import ru.sovaowltv.service.data.DataExtractor;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImpl userDetailsService;

    private final DataExtractor dataExtractor;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .ignoringAntMatchers(
                        "/profile/email/verification/**",
                        "/qiwi/success",
                        "/discord/success",
                        "/api/twitch/webhooks",
                        "/api/twitch/webhooks/**",
                        "/api/vk/callback",
                        "/api/vk/callback/**"
                );

        http.authorizeRequests()
                .antMatchers("**").permitAll();

        http.formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", false);

        http.logout()
                .logoutUrl("/logout")
                .deleteCookies("SESSION")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login?logout=true");

        http.addFilter(new MyUsernamePasswordAuthenticationFilter(authenticationManager(), dataExtractor));

        http.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> response.sendRedirect("/accessDenied");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }
}

