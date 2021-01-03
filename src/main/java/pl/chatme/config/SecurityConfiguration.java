package pl.chatme.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
import pl.chatme.security.AuthenticationFailureHandler;
import pl.chatme.security.AuthenticationSuccessHandler;
import pl.chatme.security.jwt.JWTAuthenticationFilter;
import pl.chatme.security.jwt.JWTAuthorizationFilter;
import pl.chatme.security.jwt.TokenProvider;

@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String AUTHENTICATE_ENDPOINT = "/authenticate";
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(AuthenticationSuccessHandler authenticationSuccessHandler,
                                 AuthenticationFailureHandler authenticationFailureHandler,
                                 TokenProvider tokenProvider,
                                 ObjectMapper objectMapper,
                                 SecurityProblemSupport problemSupport) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.problemSupport = problemSupport;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off

        http
            .csrf()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.POST,AUTHENTICATE_ENDPOINT).permitAll()
            .mvcMatchers(HttpMethod.POST,"/register").permitAll()
            .anyRequest().authenticated()
        .and()
            .httpBasic()
        .and()
            .addFilter(authenticationFilter())
            .addFilter(new JWTAuthorizationFilter(authenticationManager(),tokenProvider))
            .exceptionHandling()
            .authenticationEntryPoint(problemSupport)
            .accessDeniedHandler(problemSupport);

        // @formatter:on
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTAuthenticationFilter authenticationFilter() throws Exception {
        var filter = new JWTAuthenticationFilter(objectMapper);

        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        filter.setAuthenticationManager(authenticationManager());
        filter.setFilterProcessesUrl(AUTHENTICATE_ENDPOINT);

        return filter;
    }


}
