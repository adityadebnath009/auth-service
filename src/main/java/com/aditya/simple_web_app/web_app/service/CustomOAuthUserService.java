package com.aditya.simple_web_app.web_app.service;

import com.aditya.simple_web_app.web_app.Domain.AuthProvider;
import com.aditya.simple_web_app.web_app.Domain.Role;
import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.dto.OAuthUserInfo;
import com.aditya.simple_web_app.web_app.repository.RoleRepository;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuthUserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);



        System.out.println("OAuth attributes: " + oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" or "github"
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        OAuthUserInfo info = OAuthUserInfoFactory.createOAuthUserInfo(provider, oAuth2User.getAttributes());
        if (info.email() == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    "Email not available from " + registrationId + ". Please make your email public."
            );
        }

        User user = userRepository.findByEmail(info.email())
                .map(existing -> updateExistingUser(existing, info))
                .orElseGet(() -> createNewUser(info, provider));

        if (oAuth2User instanceof OidcUser oidcUser) {
            return new CustomUserDetails(user, oAuth2User.getAttributes(),
                    oidcUser.getIdToken(), oidcUser.getUserInfo());
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());




    }
    private User createNewUser(OAuthUserInfo info, AuthProvider provider) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));

        User user = User.builder()
                .email(info.email())
                .name(info.name())
                .profilePicture(info.profilePicture())
                .authenticationType(provider)
                .providerId(info.providerId())
                .password(null)
                .enabled(true)
                .emailVerified(true)
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(user);
    }
    private User updateExistingUser(User user, OAuthUserInfo info) {
        user.setName(info.name());
        user.setProfilePicture(info.profilePicture());
        return userRepository.save(user);
    }

}
