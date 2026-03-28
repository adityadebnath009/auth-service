package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User, OidcUser {



    private final User user;
    private  OidcIdToken idToken;
    private OidcUserInfo userInfo;
    private  Map<String, Object> attributes;
    public CustomUserDetails(User user) {
        this.user = user;

    }

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }


    public CustomUserDetails(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {


        this.user = user;
        this.idToken = idToken;
        this.userInfo = userInfo;
        this.attributes = attributes;
    }


    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
    public User getUser() {
        return user;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().
                stream().
                map(role ->
                        new SimpleGrantedAuthority(role.getName())).
                toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword() ;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled() && user.isEmailVerified();
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
