package com.freedom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // 注入WebSecurityConfig配置的AuthenticationManager
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource druidDataSource;

    /**
     * OAuth客户端配置
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("fooClient")
                .secret(passwordEncoder.encode("fooSecret"))
                .redirectUris("http://apigateway.test.com:9041/oauthLogin")
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("fooScope")
                .autoApprove(true)
                .and()
                .withClient("sso-client-2")
                .secret(passwordEncoder.encode("sso-secret-2"))
                .redirectUris("http://ssoclient2.test.com:10001/login")
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
     * 授权服务器端点配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            .tokenStore(redisTokenStore())
            .authenticationManager(authenticationManager)
            .authorizationCodeServices(new JdbcAuthorizationCodeServices(druidDataSource));  //授权码 Jdbc存储
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


    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Redis存储令牌
     * @return
     */
    @Bean
    public TokenStore redisTokenStore(){
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        redisTokenStore.setPrefix(applicationName + ":");
        return redisTokenStore;
    }

}
