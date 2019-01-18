package nl.thehyve.gb.backend

import groovy.util.logging.Slf4j
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy

@Slf4j
@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider())
    }

    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        new NullAuthenticatedSessionStrategy()
    }

    /**
     * To read keycloak spring adapter settings from application.yml
     * @return KeycloakConfigResolver for Spring Boot adapter.
     */
    @Bean
    KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver()
    }

    /**
     * Api request URI and their mapping roles and access are configured in this method.
     * This is method from spring security web configuration Override this method to configure
     * the HttpSecurity.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http)
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/error").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
    }

}
