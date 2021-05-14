package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.*;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.yggdrasil.request.AuthenticationRequest;
import com.mojang.authlib.yggdrasil.request.RefreshRequest;
import com.mojang.authlib.yggdrasil.request.ValidateRequest;
import com.mojang.authlib.yggdrasil.response.AuthenticationResponse;
import com.mojang.authlib.yggdrasil.response.RefreshResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.authlib.yggdrasil.response.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class YggdrasilUserAuthentication extends HttpUserAuthentication {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String BASE_URL = "https://authserver.mojang.com/";
    private static final URL ROUTE_AUTHENTICATE = HttpAuthenticationService.constantURL(BASE_URL + "authenticate");
    private static final URL ROUTE_REFRESH = HttpAuthenticationService.constantURL(BASE_URL + "refresh");
    private static final URL ROUTE_VALIDATE = HttpAuthenticationService.constantURL(BASE_URL + "validate");
    private static final URL ROUTE_INVALIDATE = HttpAuthenticationService.constantURL(BASE_URL + "invalidate");
    private static final URL ROUTE_SIGNOUT = HttpAuthenticationService.constantURL(BASE_URL + "signout");
    private static final String STORAGE_KEY_ACCESS_TOKEN = "accessToken";

    private final Agent agent;
    private GameProfile[] profiles;
    private String accessToken;
    private boolean isOnline;

    public YggdrasilUserAuthentication(YggdrasilAuthenticationService authenticationService, Agent agent) {
        super(authenticationService);
        this.agent = agent;
    }

    public boolean canLogIn() {
        return !this.canPlayOnline() && StringUtils.isNotBlank(this.getUsername()) && (StringUtils.isNotBlank(this.getPassword()) || StringUtils.isNotBlank(this.getAuthenticatedToken()));
    }

    public void logIn() throws AuthenticationException {
        if (StringUtils.isBlank(this.getUsername())) {
            throw new InvalidCredentialsException("Invalid username");
        }

        if (StringUtils.isNotBlank(this.getAuthenticatedToken())) {
            this.logInWithToken();
        } else if (StringUtils.isNotBlank(this.getPassword())) {
            this.logInWithPassword();
        } else {
            throw new InvalidCredentialsException("Invalid password");
        }
    }

    protected void logInWithPassword() throws AuthenticationException {
        if (StringUtils.isBlank(this.getUsername())) {
            throw new InvalidCredentialsException("Invalid username");
        }

        if (StringUtils.isBlank(this.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        LOGGER.info("Logging in with username & password");

        AuthenticationRequest request = new AuthenticationRequest(this, this.getUsername(), this.getPassword());
        AuthenticationResponse response = this.getAuthenticationService().makeRequest(ROUTE_AUTHENTICATE, request, AuthenticationResponse.class);
        if (!response.getClientToken().equals(this.getAuthenticationService().getClientToken())) {
            throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
        }

        if (response.getSelectedProfile() != null) {
            this.setUserType(response.getSelectedProfile().isLegacy() ? UserType.LEGACY : UserType.MOJANG);
        } else if (ArrayUtils.isNotEmpty(response.getAvailableProfiles())) {
            this.setUserType(response.getAvailableProfiles()[0].isLegacy() ? UserType.LEGACY : UserType.MOJANG);
        }

        User user = response.getUser();
        if (user != null && user.getId() != null) {
            this.setUserId(user.getId());
        } else {
            this.setUserId(this.getUsername());
        }

        this.isOnline = true;
        this.accessToken = response.getAccessToken();
        this.profiles = response.getAvailableProfiles();
        this.setSelectedProfile(response.getSelectedProfile());
        this.getModifiableUserProperties().clear();
        this.updateUserProperties(user);
    }

    protected void updateUserProperties(User user) {
        if (user == null) {
            return;
        }

        if (user.getProperties() != null) {
            this.getModifiableUserProperties().putAll(user.getProperties());
        }
    }

    protected void logInWithToken() throws AuthenticationException {
        if (StringUtils.isBlank(this.getUserId())) {
            if (StringUtils.isBlank(this.getUsername())) {
                this.setUserId(this.getUsername());
            } else {
                throw new InvalidCredentialsException("Invalid uuid & username");
            }
        }

        if (StringUtils.isBlank(this.getAuthenticatedToken())) {
            throw new InvalidCredentialsException("Invalid access token");
        }

        LOGGER.info("Logging in with access token");

        if (this.checkTokenValidity()) {
            LOGGER.debug("Skipping refresh call as we're safely logged in.");
            this.isOnline = true;
            return;
        }

        RefreshRequest request = new RefreshRequest(this);
        RefreshResponse response = this.getAuthenticationService().makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
        if (!response.getClientToken().equals(this.getAuthenticationService().getClientToken())) {
            throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
        }

        if (response.getSelectedProfile() != null) {
            this.setUserType(response.getSelectedProfile().isLegacy() ? UserType.LEGACY : UserType.MOJANG);
        } else if (ArrayUtils.isNotEmpty(response.getAvailableProfiles())) {
            this.setUserType(response.getAvailableProfiles()[0].isLegacy() ? UserType.LEGACY : UserType.MOJANG);
        }

        if (response.getUser() != null && response.getUser().getId() != null) {
            this.setUserId(response.getUser().getId());
        } else {
            this.setUserId(this.getUsername());
        }

        this.isOnline = true;
        this.accessToken = response.getAccessToken();
        this.profiles = response.getAvailableProfiles();
        this.setSelectedProfile(response.getSelectedProfile());
        this.getModifiableUserProperties().clear();
        this.updateUserProperties(response.getUser());
    }

    protected boolean checkTokenValidity() throws AuthenticationException {
        try {
            this.getAuthenticationService().makeRequest(ROUTE_VALIDATE, new ValidateRequest(this), Response.class);
            return true;
        } catch (AuthenticationException ex) {
            return false;
        }
    }

    public void logOut() {
        super.logOut();

        this.accessToken = null;
        this.profiles = null;
        this.isOnline = false;
    }

    public GameProfile[] getAvailableProfiles() {
        return this.profiles;
    }

    public boolean isLoggedIn() {
        return StringUtils.isNotBlank(this.accessToken);
    }

    public boolean canPlayOnline() {
        return this.isLoggedIn() && this.getSelectedProfile() != null && this.isOnline;
    }

    public void selectGameProfile(GameProfile profile) throws AuthenticationException {
        if (!this.isLoggedIn()) {
            throw new AuthenticationException("Cannot change game profile whilst not logged in");
        }

        if (this.getSelectedProfile() != null) {
            throw new AuthenticationException("Cannot change game profile. You must log out and back in.");
        }

        if (profile == null || !ArrayUtils.contains(this.profiles, profile)) {
            throw new IllegalArgumentException("Invalid profile '" + profile + "'");
        }

        RefreshRequest request = new RefreshRequest(this, profile);
        RefreshResponse response = this.getAuthenticationService().makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);
        if (!response.getClientToken().equals(this.getAuthenticationService().getClientToken())) {
            throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
        }

        this.isOnline = true;
        this.accessToken = response.getAccessToken();
        this.setSelectedProfile(response.getSelectedProfile());
    }

    public void loadFromStorage(Map<String, Object> credentials) {
        super.loadFromStorage(credentials);
        this.accessToken = String.valueOf(credentials.get(STORAGE_KEY_ACCESS_TOKEN));
    }

    public Map<String, Object> saveForStorage() {
        Map<String, Object> result = super.saveForStorage();

        if (StringUtils.isNotBlank(this.getAuthenticatedToken())) {
            result.put(STORAGE_KEY_ACCESS_TOKEN, this.getAuthenticatedToken());
        }

        return result;
    }

    @Deprecated
    public String getSessionToken() {
        if (this.isLoggedIn() && this.getSelectedProfile() != null && this.canPlayOnline()) {
            return String.format("token:%s:%s", this.getAuthenticatedToken(), this.getSelectedProfile().getId());
        }

        return null;
    }

    public String getAuthenticatedToken() {
        return this.accessToken;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public String toString() {
        return "YggdrasilAuthenticationService{agent=" + this.agent + ", profiles=" + Arrays.toString(this.profiles) + ", selectedProfile=" + this.getSelectedProfile() + ", username='" + this.getUsername() + '\'' + ", isLoggedIn=" + this.isLoggedIn() + ", userType=" + this.getUserType() + ", canPlayOnline=" + this.canPlayOnline() + ", accessToken='" + this.accessToken + '\'' + ", clientToken='" + this.getAuthenticationService().getClientToken() + '\'' + '}';
    }

    public YggdrasilAuthenticationService getAuthenticationService() {
        return (YggdrasilAuthenticationService) super.getAuthenticationService();
    }

}
