# spring-security-oauth-sso-poc
基于Spring Security OAuth2的SSO单点登录POC，包含【单体SpringBoot项目】 和 【基于Zuul API网关+后端API微服务】 两种架构的子系统接入

<br>

## 项目

- api-gateway-zuul： API网关，既是调用API的入口，又是和SPA维护登录状态的Web Server，充当OAuth Client角色
- authorization-server： 授权服务器，颁发accessToken、refreshToken
- resource-server-mvc： 资源服务器，隐藏在zuul网关之后
- service-registry： Eureka注册中心
- sso-client： springboot web项目，集成单点登录

分为 springboot 1.x 和 springboot 2.x 两个版本

<br>

## 参考

- [OAuth2 – @EnableResourceServer vs @EnableOAuth2Sso](https://www.baeldung.com/spring-security-oauth2-enable-resource-server-vs-enable-oauth2-sso)
- https://github.com/Baeldung/oauth-microservices

