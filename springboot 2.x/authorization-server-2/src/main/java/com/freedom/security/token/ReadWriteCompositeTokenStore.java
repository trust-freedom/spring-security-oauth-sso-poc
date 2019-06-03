package com.freedom.security.token;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Collection;
import java.util.List;

/**
 * 读写分离的TokenStore
 * 负责读/写的TokenStore各是一个集合
 * 写的需要集合中所有TokenStore都写成功才返回，读的只要第一个读到的就返回（故需要注意顺序）
 */
public class ReadWriteCompositeTokenStore implements TokenStore {

    // 负责读的TokenStores
    private final List<TokenStore> readTokenStores;

    // 负责写的TokenStores
    private final List<TokenStore> writeTokenStores;


    public ReadWriteCompositeTokenStore(List<TokenStore> readTokenStores, List<TokenStore> writeTokenStores) {
        this.readTokenStores = readTokenStores;
        this.writeTokenStores = writeTokenStores;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(token);
                if(oAuth2Authentication != null){
                    return oAuth2Authentication;
                }
            }
        }
        return null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        if(writeTokenStores != null){
            for(TokenStore tokenStore : writeTokenStores){
                tokenStore.storeAccessToken(token, authentication);
            }
        }
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(tokenValue);
                if(oAuth2AccessToken != null){
                    return oAuth2AccessToken;
                }
            }
        }
        return null;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        if(writeTokenStores != null){
            for(TokenStore tokenStore : writeTokenStores){
                tokenStore.removeAccessToken(token);
            }
        }
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        if(writeTokenStores != null){
            for(TokenStore tokenStore : writeTokenStores){
                tokenStore.storeRefreshToken(refreshToken, authentication);
            }
        }
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                OAuth2RefreshToken oAuth2RefreshToken = tokenStore.readRefreshToken(tokenValue);
                if(oAuth2RefreshToken != null){
                    return oAuth2RefreshToken;
                }
            }
        }
        return null;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                OAuth2Authentication oAuth2Authentication = tokenStore.readAuthenticationForRefreshToken(token);
                if(oAuth2Authentication != null){
                    return oAuth2Authentication;
                }
            }
        }
        return null;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        if(writeTokenStores != null){
            for(TokenStore tokenStore : writeTokenStores){
                tokenStore.removeRefreshToken(token);
            }
        }
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        if(writeTokenStores != null){
            for(TokenStore tokenStore : writeTokenStores){
                tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);
            }
        }
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                OAuth2AccessToken oAuth2AccessToken = tokenStore.getAccessToken(authentication);
                if(oAuth2AccessToken != null){
                    return oAuth2AccessToken;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                Collection<OAuth2AccessToken> accessTokens = tokenStore.findTokensByClientIdAndUserName(clientId, userName);
                if(accessTokens != null){
                    return accessTokens;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        if(readTokenStores != null){
            for(TokenStore tokenStore : readTokenStores){
                Collection<OAuth2AccessToken> accessTokens = tokenStore.findTokensByClientId(clientId);
                if(accessTokens != null){
                    return accessTokens;
                }
            }
        }
        return null;
    }
}
