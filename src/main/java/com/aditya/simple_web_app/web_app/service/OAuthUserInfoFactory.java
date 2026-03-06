package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.AuthProvider;
import com.aditya.simple_web_app.web_app.dto.OAuthUserInfo;

import java.util.Map;

public class OAuthUserInfoFactory {

    public static OAuthUserInfo createOAuthUserInfo(AuthProvider authProvider, Map<String, Object> properties) {


        return switch (authProvider)
        {
            case GOOGLE ->  new OAuthUserInfo(
                    (String)properties.get("sub"),
                    (String)properties.get("email"),
                    (String)properties.get("name"),
                    (String)properties.get("picture")
            );
            case GITHUB -> new OAuthUserInfo(
                    (String)properties.get("id"),
                    (String)properties.get("email"),
                    (String)properties.get("name"),
                    (String)properties.get("picture")

            );
            default -> throw new IllegalArgumentException("Invalid auth provider");
        };
    }

}
