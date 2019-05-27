package com.freedom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // 注入WebSecurityConfig配置的AuthenticationManager
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 配置 ClientDetailsService
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("fooClient").secret(passwordEncoder.encode("fooSecret"))
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("fooScope")
                .autoApprove(true)
                .and()
                .withClient("sso-client-2")
                .secret(passwordEncoder.encode("sso-secret-2"))
                //.redirectUris("http://ssoclient1.com:10001/login")
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("sso-client2-scope")
                .autoApprove(true);
    }

    /**
     * 配置 AuthorizationServerSecurity
     * @param oauthServer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 配置 AuthorizationServerEndpoints
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            //.tokenStore(tokenStore())
            .authenticationManager(authenticationManager);
            //.accessTokenConverter(jwtAccessTokenConverter());
    }

    //@Bean
    //public TokenStore tokenStore() {
    //    return new JwtTokenStore(jwtAccessTokenConverter());
    //}

    //@Bean
    //public JwtAccessTokenConverter jwtAccessTokenConverter() {
    //    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    //    converter.setSigningKey("abc");
    //    return converter;
    //}

}
