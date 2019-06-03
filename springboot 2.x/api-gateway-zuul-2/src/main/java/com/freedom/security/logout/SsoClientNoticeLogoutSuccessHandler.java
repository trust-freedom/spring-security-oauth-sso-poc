package com.freedom.security.logout;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * SSO Client使用，用于通知授权服务器退出的Handler
 */
public class SsoClientNoticeLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(SsoClientNoticeLogoutSuccessHandler.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private RestTemplate restTemplate;

    public SsoClientNoticeLogoutSuccessHandler(){}

    //public SsoClientNoticeLogoutSuccessHandler(String jwtSecret) {
    //    this.jwtSecret = jwtSecret;
    //}


    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 调用授权服务器/logout端点，使用JWT封装用户信息
        Authentication userAuthentication = ((OAuth2Authentication)authentication).getUserAuthentication();

        String username = (String)userAuthentication.getPrincipal();
        List<String> roles = new ArrayList<>();
        Collection<? extends GrantedAuthority> authorities = userAuthentication.getAuthorities();
        if(authorities!=null && authorities.size()>0){
            for(GrantedAuthority authority : authorities){
                roles.add(authority.getAuthority());
            }
        }

        String jwt = Jwts.builder()
                .setSubject(username)
                .claim("userRoles", roles)
                .claim("username", username)
                .setIssuedAt(new Date())
                //.setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, TextCodec.BASE64.encode(jwtSecret))  // TODO 必须要base64？？
                .compact();

        logger.info(restTemplate+"");

        MultiValueMap<String, String> header = new HttpHeaders();
        header.add("sso_auto_logout_jwt", jwt);
        HttpEntity httpEntity = new HttpEntity("", header);
        String result = restTemplate.postForObject("http://authserver.test.com:8765/authorization-server-2/ssoAutoLogout",
                httpEntity,String.class);
        logger.info("result==" + result);
    }
}
