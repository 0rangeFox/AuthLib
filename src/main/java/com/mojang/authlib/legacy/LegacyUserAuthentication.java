package com.mojang.authlib.legacy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.HttpUserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.util.UUIDTypeAdapter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class LegacyUserAuthentication extends HttpUserAuthentication {

    private static final URL AUTHENTICATION_URL = HttpAuthenticationService.constantURL("https://login.minecraft.net");
    private static final int AUTHENTICATION_VERSION = 14;
    private static final int RESPONSE_PART_PROFILE_NAME = 2;
    private static final int RESPONSE_PART_SESSION_TOKEN = 3;
    private static final int RESPONSE_PART_PROFILE_ID = 4;
    private String sessionToken;

    protected LegacyUserAuthentication(LegacyAuthenticationService authenticationService) {
        super(authenticationService);
    }

    public void logIn() throws AuthenticationException {
        String sessionToken, profileName, profileId, response;

        if (StringUtils.isBlank(this.getUsername())) {
            throw new InvalidCredentialsException("Invalid username");
        }
        if (StringUtils.isBlank(this.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("user", this.getUsername());
        arguments.put("password", this.getPassword());
        arguments.put("version", AUTHENTICATION_VERSION);
        try {
            response = this.getAuthenticationService().performPostRequest(AUTHENTICATION_URL, HttpAuthenticationService.buildQuery(arguments), "application/x-www-form-urlencoded").trim();
        } catch (IOException e) {
            throw new AuthenticationException("Authentication server is not responding", e);
        }

        String[] split = response.split(":");
        if (split.length == 5) {
            profileName = split[2];
            sessionToken = split[3];
            profileId = split[4];

            if (StringUtils.isBlank(profileId) || StringUtils.isBlank(profileName) || StringUtils.isBlank(sessionToken)) {
                throw new AuthenticationException("Unknown response from authentication server: " + response);
            }
        } else {
            throw new InvalidCredentialsException(response);
        }

        this.setSelectedProfile(new GameProfile(UUIDTypeAdapter.fromString(profileId), profileName));
        this.sessionToken = sessionToken;
        this.setUserType(UserType.LEGACY);
    }

    public void logOut() {
        super.logOut();
        this.sessionToken = null;
    }

    public boolean canPlayOnline() {
        return this.isLoggedIn() && this.getSelectedProfile() != null && this.getAuthenticatedToken() != null;
    }

    public GameProfile[] getAvailableProfiles() {
        return this.getSelectedProfile() != null ? new GameProfile[]{ this.getSelectedProfile() } : new GameProfile[0];
    }

    public void selectGameProfile(GameProfile profile) throws AuthenticationException {
        throw new UnsupportedOperationException("Game profiles cannot be changed in the legacy authentication service");
    }

    public String getAuthenticatedToken() {
        return this.sessionToken;
    }

    public String getUserId() {
        return this.getUsername();
    }

    public LegacyAuthenticationService getAuthenticationService() {
        return (LegacyAuthenticationService) super.getAuthenticationService();
    }

}
