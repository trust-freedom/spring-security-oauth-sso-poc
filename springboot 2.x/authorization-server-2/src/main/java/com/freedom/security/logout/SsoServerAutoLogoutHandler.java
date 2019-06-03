package com.freedom.security.logout;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 1、通过JWT得到username，再找到对应的sessionId，清除session
 * 2、异步通知所有SSO Client登出
 */
public class SsoServerAutoLogoutHandler implements LogoutHandler {
    private static Logger logger = LoggerFactory.getLogger(SsoServerAutoLogoutHandler.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private RestTemplate restTemplate;

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

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String jwt = extractHeaderJwtToken(request);

        if(StringUtils.isEmpty(jwt)){
            logger.warn("SSO Server auto logout JWT is null");
        }

        // 检查 TODO 抽取
        Jwts.parser().setSigningKey(TextCodec.BASE64.encode(jwtSecret)).parseClaimsJws(jwt);

        Claims claims = Jwts.parser()
                .setSigningKey(TextCodec.BASE64.encode(jwtSecret))
                .parseClaimsJws(jwt)
                .getBody();

        String username = claims.get("username", String.class);

        Map<String, ? extends Session> usersSessions =
                sessionRepository.findByIndexNameAndIndexValue(
                        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                        username);

        Set<String> keySet = usersSessions.keySet();
        if(keySet!=null && !keySet.isEmpty()){
            Iterator iterator = keySet.iterator();
            while(iterator.hasNext()){
                String sessionId = (String)iterator.next();
                sessionRepository.deleteById(sessionId);
            }
        }

        // 异步通知其它客户端logout
        MultiValueMap<String, String> header = new HttpHeaders();
        header.add("sso_auto_logout_jwt", jwt);
        HttpEntity httpEntity = new HttpEntity("", header);

        try {
            String result = restTemplate.postForObject("http://ssoclient2.test.com:10001/ssoAutoLogout",
                    httpEntity,String.class);
            logger.info("result==" + result);
        }
        catch(Exception e){
            logger.error("", e);
        }

        try {
            String result = restTemplate.postForObject("http://apigateway.test.com:9041/ssoAutoLogout",
                    httpEntity,String.class);
            logger.info("result==" + result);
        }
        catch(Exception e){
            logger.error("", e);
        }

    }


    private String extractHeaderJwtToken(HttpServletRequest request){
        Enumeration<String> headers = request.getHeaders("sso_auto_logout_jwt");
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();

            if(StringUtils.hasText(value)){
                return value;
            }
        }


        return null;
    }
}
