package com.freedom.security.config;

import com.freedom.security.logout.SsoServerAutoLogoutHandler;
import com.freedom.security.logout.SsoServerAutoLogoutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SsoServerAutoLogoutConfig {

    @Bean
    public LogoutHandler ssoServerAutoLogoutHandler(){
        return new SsoServerAutoLogoutHandler();
    }

    @Bean
    public LogoutFilter ssoAutoLogoutFilter(){
        List<LogoutHandler> logoutHandlers = new ArrayList<>();
        logoutHandlers.add(ssoServerAutoLogoutHandler()); // 负责清理session，异步通知SSO Client
        LogoutHandler[] handlers = logoutHandlers.toArray(new LogoutHandler[logoutHandlers.size()]);

        LogoutFilter logoutFilter = new LogoutFilter(new SsoServerAutoLogoutSuccessHandler(), handlers);
        logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("/ssoAutoLogout", "POST"));

        return logoutFilter;
    }

}
