package com.freedom.security.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SaveAccessTokenToCookieAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements InitializingBean {

    // 登录成功后页面
    @Value("${app.loginSuccessPage}")
    private String loginSuccessPage;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setDefaultTargetUrl(loginSuccessPage);
    }


    @Autowired(required = false)
    private OAuth2ClientContext clientContext;


    /**
     * 认证成功后的处理
     * @param request
     * @param response
     * @param authentication
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        //
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication;
        //oAuth2Authentication.

        String accessToken = clientContext!=null ? clientContext.getAccessToken().getValue() : "";
        //String accessToken = ((OAuth2AuthenticationDetails)oAuth2Authentication.getDetails()).getTokenValue();

        // access tokne 放入cookie
        Cookie cookie = new Cookie("access_token_cookie", accessToken);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        // 调用父类，实现跳转
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
