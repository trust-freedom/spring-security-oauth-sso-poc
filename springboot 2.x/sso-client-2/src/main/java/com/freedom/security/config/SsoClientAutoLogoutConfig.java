package com.freedom.security.config;

import com.freedom.security.logout.SsoClientAutoLogoutHandler;
import com.freedom.security.logout.SsoClientAutoLogoutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SsoClientAutoLogoutConfig {

    @Bean
    public LogoutHandler ssoClientAutoLogoutHandler(){
        return new SsoClientAutoLogoutHandler();
    }

    @Bean
    public LogoutFilter ssoAutoLogoutFilter(){
        List<LogoutHandler> logoutHandlers = new ArrayList<>();
        logoutHandlers.add(ssoClientAutoLogoutHandler()); // 负责清理session
        LogoutHandler[] handlers = logoutHandlers.toArray(new LogoutHandler[logoutHandlers.size()]);

        LogoutFilter logoutFilter = new LogoutFilter(new SsoClientAutoLogoutSuccessHandler(), handlers);
        logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("/ssoAutoLogout", "POST"));

        return logoutFilter;
    }

}
