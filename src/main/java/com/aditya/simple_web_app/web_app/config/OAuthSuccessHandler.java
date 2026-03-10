package com.aditya.simple_web_app.web_app.config;


import com.aditya.simple_web_app.web_app.Domain.User;
import com.aditya.simple_web_app.web_app.repository.UserRepository;
import com.aditya.simple_web_app.web_app.service.CustomUserDetails;
import com.aditya.simple_web_app.web_app.service.RefreshTokenService;
import com.aditya.simple_web_app.web_app.util.TokenService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public OAuthSuccessHandler(TokenService tokenService, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException, java.io.IOException
    {


        String email = extractEmail(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateRefreshToken(userDetails);

        refreshTokenService.createRefreshToken(userDetails.getUser(), refreshToken, request.getHeader("User-Agent"), request.getRemoteAddr());
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)   // false for local dev, true in production
                .path("/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax") // Lax because OAuth involves cross-site redirects
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // Step 5 — redirect frontend with access token in URL

        getRedirectStrategy().sendRedirect(request, response,
                "http://localhost:5173/oauth/callback?token=" + accessToken);


    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        } else if (principal instanceof OAuth2User oAuth2User) {
            return (String) oAuth2User.getAttribute("email");
        }
        throw new RuntimeException("Unknown principal type: " + principal.getClass());
    }

}
