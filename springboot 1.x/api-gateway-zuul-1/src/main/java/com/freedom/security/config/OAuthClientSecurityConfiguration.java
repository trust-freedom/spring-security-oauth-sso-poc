package com.freedom.security.config;

import com.freedom.security.filter.CookieTokenExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerTokenServicesConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import java.util.Collections;

@Configuration
@EnableOAuth2Client
@Import(ResourceServerTokenServicesConfiguration.class)  // @EnableResourceServer后其实就导入了ResourceServerTokenServicesConfiguration
//@EnableResourceServer
//@Order(value = SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class OAuthClientSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.oauth2.sso.loginPath}")
    private String ssoLoginPath;

    @Autowired
    private UserInfoRestTemplateFactory userInfoRestTemplateFactory;

    @Autowired
    private ResourceServerTokenServices tokenServices;

    @Autowired
    private SaveAccessTokenToCookieAuthenticationSuccessHandler saveAccessTokenToCookieAuthenticationSuccessHandler;

    private OAuth2ClientAuthenticationProcessingFilter oauth2SsoFilter() {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(ssoLoginPath);
        filter.setRestTemplate(userInfoRestTemplateFactory.getUserInfoRestTemplate());
        filter.setTokenServices(tokenServices);
        filter.setApplicationEventPublisher(this.getApplicationContext());

        // 自定义AuthenticationSuccessHandler
        filter.setAuthenticationSuccessHandler(saveAccessTokenToCookieAuthenticationSuccessHandler);

        // 直接从httpSecurity.getSharedObject() 为null
        // 是 SessionManagementConfigurer配置的默认SessionAuthenticationStrategy
        // WebSecurityConfigurerAdapter的配置在前，SessionManagementConfigurer这种SecurityConfigurerAdapter的配置在后，所以此时还未配置
        //filter.setSessionAuthenticationStrategy(httpSecurity.getSharedObject(SessionAuthenticationStrategy.class));

        return filter;
    }

    /**
     * 使用SecurityConfigurer为filter设置SessionAuthenticationStrategy，此时已经经过SessionManagementConfigurer设置SessionAuthenticationStrategy
     * 1、给OAuth2ClientAuthenticationProcessingFilter配置SessionAuthenticationStrategy
     * 2、将OAuth2ClientAuthenticationProcessingFilter添加到过滤器链
     */
    private static class OAuth2ClientAuthenticationConfigurer
            extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

        private OAuth2ClientAuthenticationProcessingFilter filter;

        OAuth2ClientAuthenticationConfigurer(OAuth2ClientAuthenticationProcessingFilter filter) {
            this.filter = filter;
        }

        @Override
        public void configure(HttpSecurity httpSecurity) throws Exception {
            OAuth2ClientAuthenticationProcessingFilter ssoFilter = this.filter;

            ssoFilter.setSessionAuthenticationStrategy(httpSecurity.getSharedObject(SessionAuthenticationStrategy.class));
            httpSecurity.addFilterAfter(ssoFilter, AbstractPreAuthenticatedProcessingFilter.class);
        }
    }

    /**
     * OAuth登录的AuthenticationEntryPoint
     * @param httpSecurity
     * @throws Exception
     */
    private void addAuthenticationEntryPoint(HttpSecurity httpSecurity) throws Exception {
        ExceptionHandlingConfigurer<HttpSecurity> exceptions = httpSecurity.exceptionHandling();
        ContentNegotiationStrategy contentNegotiationStrategy = httpSecurity.getSharedObject(ContentNegotiationStrategy.class);
        if (contentNegotiationStrategy == null) {
            contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
        }
        MediaTypeRequestMatcher preferredMatcher = new MediaTypeRequestMatcher(
                contentNegotiationStrategy, MediaType.APPLICATION_XHTML_XML,
                new MediaType("image", "*"), MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
        preferredMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        exceptions.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(ssoLoginPath),
                preferredMatcher);
        // When multiple entry points are provided the default is the first one
        exceptions.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"));
    }


    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  //服务器端无状态，即不使用session存储SecurityContext，RequestCatche等
                    .and()
                //.formLogin()
                //    .loginPage("/oauthLogin")  // oauthLogin作为登录入口
                //    .and()
                .authorizeRequests()
                    //.antMatchers("/authorization-server-1/**", "/login").permitAll()
                    .antMatchers("/oauthLogin").permitAll()  //OAuth登录链接是不需要认证即可访问的
                    .anyRequest().authenticated()
                    .and()
                .addFilterBefore(oAuth2AuthenticationProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .logout().permitAll().logoutSuccessUrl("/");

        // 添加OAuth2 Client相关配置
        httpSecurity.apply(new OAuth2ClientAuthenticationConfigurer(oauth2SsoFilter()));

        // 添加跳转到 security.oauth2.sso.loginPath 的 AuthenticationEntryPoint
        addAuthenticationEntryPoint(httpSecurity);
    }


    @Autowired
    private ResourceServerTokenServices resourceServerTokenServices;

    /**
     * 创建校验access token的过滤器 OAuth2AuthenticationProcessingFilter
     * @return
     */
    private OAuth2AuthenticationProcessingFilter oAuth2AuthenticationProcessingFilter() {
        OAuth2AuthenticationProcessingFilter oAuth2AuthenticationProcessingFilter = new OAuth2AuthenticationProcessingFilter();
        oAuth2AuthenticationProcessingFilter.setAuthenticationManager(oauthAuthenticationManager());
        oAuth2AuthenticationProcessingFilter.setStateless(true);

        // 从cookie中提取token
        oAuth2AuthenticationProcessingFilter.setTokenExtractor(new CookieTokenExtractor());

        return oAuth2AuthenticationProcessingFilter;
    }

    /**
     *
     * @return
     */
    private AuthenticationManager oauthAuthenticationManager() {
        OAuth2AuthenticationManager oAuth2AuthenticationManager = new OAuth2AuthenticationManager();
        oAuth2AuthenticationManager.setResourceId("apigateway");
        oAuth2AuthenticationManager.setTokenServices(resourceServerTokenServices);
        oAuth2AuthenticationManager.setClientDetailsService(null);

        return oAuth2AuthenticationManager;
    }



}
