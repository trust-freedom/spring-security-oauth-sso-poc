package com.freedom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("fooClient")
                .secret("fooSecret")
                .redirectUris("http://apigateway.com:9041/oauthLogin")
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("fooScope")
                .autoApprove(true)
                .and()
                .withClient("sso-client-1")
                .secret("sso-secret-1")
                .redirectUris("http://ssoclient1.com:10001/login")
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("sso-client1-scope")
                .autoApprove(true);  // 所有scope自动授权
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer authorizationServerSecurityConfigurer) throws Exception {
        authorizationServerSecurityConfigurer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");  // check_token端点可访问，但需认证
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer authorizationServerEndpointsConfigurer) throws Exception {
        authorizationServerEndpointsConfigurer
                //.tokenStore(tokenStore())
                .authenticationManager(authenticationManager);
                //.accessTokenConverter(jwtAccessTokenConverter());
    }

    // 授权服务器不颁发JWT令牌，使用透明令牌
    //@Bean
    //public TokenStore tokenStore() {
    //    return new JwtTokenStore(jwtAccessTokenConverter());
    //}

    // 授权服务器不颁发JWT令牌，也就不需要转换
    //@Bean
    //public JwtAccessTokenConverter jwtAccessTokenConverter() {
    //    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    //    converter.setSigningKey("abc");
    //    return converter;
    //}

}