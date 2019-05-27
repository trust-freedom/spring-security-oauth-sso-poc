package com.freedom.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    //@Bean
    //FilterRegistrationBean forwardedHeaderFilter() {
    //    FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
    //    filterRegBean.setFilter(new ForwardedHeaderFilter());
    //    filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    //    return filterRegBean;
    //}

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/toLogin").setViewName("login");
        //registry.addViewController("/oauth/confirm_access").setViewName("authorize");  //不使用自定义授权页面
    }

}