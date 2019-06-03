package com.freedom.security.config;

import com.freedom.security.logout.SsoClientNoticeLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@EnableOAuth2Sso  //如果写在主启动类，@EnableOAuth2Sso注解本身就会启动一个针对/**的过滤器链，再加上当前WebSecurityConfigurerAdapter的AnyRequestMatcher，就有两个，第一个生效
@Configuration
@Order(101)
public class SsoClientSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Autowired
    private LogoutFilter ssoClientAutoLogoutHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
            .logout()
                .logoutUrl("/logout")     // 普通logout
                //.addLogoutHandler(ssoClientSsoAutoLogoutHandler())
                //.logoutSuccessUrl("")
                .logoutSuccessHandler(ssoClientNoticeLogoutSuccessHandler())  // 通知 SSO Server logout
                //.deleteCookies("")
                .permitAll()
                .and()
            .addFilterBefore(ssoClientAutoLogoutHandler, LogoutFilter.class);
    }


    @Bean
    public LogoutSuccessHandler ssoClientNoticeLogoutSuccessHandler(){
        return new SsoClientNoticeLogoutSuccessHandler();
    }

}
