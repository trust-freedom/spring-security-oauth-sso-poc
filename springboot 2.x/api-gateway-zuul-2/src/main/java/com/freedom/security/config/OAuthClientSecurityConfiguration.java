package com.freedom.security.config;

import com.freedom.security.logout.SsoClientNoticeLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@EnableOAuth2Sso
@Configuration
public class OAuthClientSecurityConfiguration extends WebSecurityConfigurerAdapter {

    // 登录成功后页面
    @Value("${app.loginSuccessPage}")
    private String loginSuccessPage;

    @Autowired
    private ResourceServerTokenServices resourceServerTokenServices;

    @Autowired
    private LogoutFilter ssoClientAutoLogoutHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception { // @formatter:off
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/oauthLogin").permitAll()
                .anyRequest().authenticated()
                .and()
            .addFilterAfter(oAuth2AuthenticationProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class)
            .logout()
                .logoutUrl("/logout")
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

    private OAuth2AuthenticationProcessingFilter oAuth2AuthenticationProcessingFilter() {
        OAuth2AuthenticationProcessingFilter oAuth2AuthenticationProcessingFilter = new OAuth2AuthenticationProcessingFilter();
        oAuth2AuthenticationProcessingFilter.setAuthenticationManager(oauthAuthenticationManager());
        oAuth2AuthenticationProcessingFilter.setStateless(false);

        return oAuth2AuthenticationProcessingFilter;
    }

    private AuthenticationManager oauthAuthenticationManager() {
        OAuth2AuthenticationManager oAuth2AuthenticationManager = new OAuth2AuthenticationManager();
        oAuth2AuthenticationManager.setResourceId("apigateway");
        oAuth2AuthenticationManager.setTokenServices(resourceServerTokenServices);
        oAuth2AuthenticationManager.setClientDetailsService(null);

        return oAuth2AuthenticationManager;
    }

}
