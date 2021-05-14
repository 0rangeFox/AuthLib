package com.mojang.authlib;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;

import java.util.Map;

public interface UserAuthentication {

    boolean canLogIn();

    void logIn() throws AuthenticationException;

    void logOut();

    boolean isLoggedIn();

    boolean canPlayOnline();

    GameProfile[] getAvailableProfiles();

    GameProfile getSelectedProfile();

    void selectGameProfile(GameProfile profile) throws AuthenticationException;

    void loadFromStorage(Map<String, Object> credentials);

    Map<String, Object> saveForStorage();

    void setUsername(String username);

    void setPassword(String password);

    String getAuthenticatedToken();

    String getUserId();

    PropertyMap getUserProperties();

    UserType getUserType();

}
