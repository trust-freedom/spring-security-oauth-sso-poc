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
 * 实现由SSO Server端触发的SSO Client自动退出，只清理Client自己的session
 */
public class SsoClientAutoLogoutHandler implements LogoutHandler {
    private static Logger logger = LoggerFactory.getLogger(SsoClientAutoLogoutHandler.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }


    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String jwt = extractHeaderJwtToken(request);

        if(StringUtils.isEmpty(jwt)){
            logger.warn("SSO Client auto logout JWT is null");
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
