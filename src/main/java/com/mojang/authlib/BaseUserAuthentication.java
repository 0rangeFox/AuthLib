package com.mojang.authlib;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseUserAuthentication implements UserAuthentication {

    protected static final String STORAGE_KEY_PROFILE_NAME = "displayName";
    protected static final String STORAGE_KEY_PROFILE_ID = "uuid";
    protected static final String STORAGE_KEY_PROFILE_PROPERTIES = "profileProperties";
    protected static final String STORAGE_KEY_USER_NAME = "username";
    protected static final String STORAGE_KEY_USER_ID = "userid";
    protected static final String STORAGE_KEY_USER_PROPERTIES = "userProperties";

    private static final Logger LOGGER = LogManager.getLogger();

    @Getter private final AuthenticationService authenticationService;
    private final PropertyMap userProperties = new PropertyMap();
    @Getter private String userId;
    @Getter(AccessLevel.PROTECTED) private String username;
    @Getter(AccessLevel.PROTECTED) private String password;
    @Getter private GameProfile selectedProfile;
    private UserType userType;

    protected BaseUserAuthentication(AuthenticationService authenticationService) {
        Validate.notNull(authenticationService);
        this.authenticationService = authenticationService;
    }

    public boolean canLogIn() {
        return !this.canPlayOnline() && StringUtils.isNotBlank(this.getUsername()) && StringUtils.isNotBlank(this.getPassword());
    }

    public void logOut() {
        this.password = null;
        this.userId = null;
        this.setSelectedProfile(null);
        this.getModifiableUserProperties().clear();
        this.setUserType(null);
    }

    public boolean isLoggedIn() {
        return this.getSelectedProfile() != null;
    }

    public void setUsername(String username) {
        if (this.isLoggedIn() && this.canPlayOnline()) {
            throw new IllegalStateException("Cannot change username whilst logged in & online");
        }

        this.username = username;
    }

    public void setPassword(String password) {
        if (this.isLoggedIn() && this.canPlayOnline() && StringUtils.isNotBlank(password)) {
            throw new IllegalStateException("Cannot set password whilst logged in & online");
        }

        this.password = password;
    }

    public void loadFromStorage(Map<String, Object> credentials) {
        this.logOut();
        this.setUsername(String.valueOf(credentials.get(STORAGE_KEY_USER_NAME)));
        this.userId = credentials.containsKey(STORAGE_KEY_USER_ID) ? String.valueOf(credentials.get(STORAGE_KEY_USER_ID)) : this.username;

        if (credentials.containsKey(STORAGE_KEY_USER_PROPERTIES)) {
            try {
                List<Map<String, Object>> list = (List<Map<String, Object>>) credentials.get(STORAGE_KEY_USER_PROPERTIES);
                for (Map<String, Object> propertyMap : list) {
                    String name = propertyMap.get("name").toString();
                    String value = propertyMap.get("value").toString();
                    String signature = propertyMap.get("signature").toString();

                    if (signature == null) {
                        this.getModifiableUserProperties().put(name, new Property(name, value));
                        continue;
                    }

                    this.getModifiableUserProperties().put(name, new Property(name, value, signature));
                }
            } catch (Throwable t) {
                LOGGER.warn("Couldn't deserialize user properties", t);
            }
        }

        if (credentials.containsKey(STORAGE_KEY_PROFILE_NAME) && credentials.containsKey(STORAGE_KEY_PROFILE_ID)) {
            GameProfile profile = new GameProfile(UUIDTypeAdapter.fromString(String.valueOf(credentials.get(STORAGE_KEY_PROFILE_ID))), String.valueOf(credentials.get(STORAGE_KEY_PROFILE_NAME)));

            if (credentials.containsKey(STORAGE_KEY_PROFILE_PROPERTIES)) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) credentials.get(STORAGE_KEY_PROFILE_PROPERTIES);
                for (Map<String, Object> propertyMap : list) {
                    String name = propertyMap.get("name").toString();
                    String value = propertyMap.get("value").toString();
                    String signature = propertyMap.get("signature").toString();

                    if (signature == null) {
                        profile.getProperties().put(name, new Property(name, value));
                        continue;
                    }

                    profile.getProperties().put(name, new Property(name, value, signature));
                }
            }

            this.setSelectedProfile(profile);
        }
    }

    public Map<String, Object> saveForStorage() {
        HashMap<String, Object> result = new HashMap<String, Object>();

        if (this.getUsername() != null) {
            result.put(STORAGE_KEY_USER_NAME, this.getUsername());
        }

        if (this.getUserId() != null) {
            result.put(STORAGE_KEY_USER_ID, this.getUserId());
        } else if (this.getUsername() != null) {
            result.put(STORAGE_KEY_USER_NAME, this.getUsername());
        }

        if (!this.getUserProperties().isEmpty()) {
            ArrayList<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();

            for (Property userProperty : this.getUserProperties().values()) {
                HashMap<String, Object> property = new HashMap<String, Object>();
                property.put("name", userProperty.getName());
                property.put("value", userProperty.getValue());
                property.put("signature", userProperty.getSignature());
                properties.add(property);
            }

            result.put(STORAGE_KEY_USER_PROPERTIES, properties);
        }

        GameProfile selectedProfile;
        if ((selectedProfile = this.getSelectedProfile()) != null) {
            result.put(STORAGE_KEY_PROFILE_NAME, selectedProfile.getName());
            result.put(STORAGE_KEY_PROFILE_ID, selectedProfile.getId());

            ArrayList<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();

            for (Property profileProperty : selectedProfile.getProperties().values()) {
                HashMap<String, Object> property = new HashMap<String, Object>();
                property.put("name", profileProperty.getName());
                property.put("value", profileProperty.getValue());
                property.put("signature", profileProperty.getSignature());
                properties.add(property);
            }

            if (!properties.isEmpty()) {
                result.put(STORAGE_KEY_PROFILE_PROPERTIES, properties);
            }
        }

        return result;
    }

    protected void setSelectedProfile(GameProfile selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getSimpleName());
        result.append("{");

        if (this.isLoggedIn()) {
            result.append("Logged in as ");
            result.append(this.getUsername());

            if (this.getSelectedProfile() != null) {
                result.append(" / ");
                result.append(this.getSelectedProfile());
                result.append(" - ");

                if (this.canPlayOnline()) {
                    result.append("Online");
                } else {
                    result.append("Offline");
                }
            }
        } else {
            result.append("Not logged in");
        }

        result.append("}");
        return result.toString();
    }

    public PropertyMap getUserProperties() {
        if (this.isLoggedIn()) {
            PropertyMap result = new PropertyMap();
            result.putAll(this.getModifiableUserProperties());
            return result;
        }

        return new PropertyMap();
    }

    protected PropertyMap getModifiableUserProperties() {
        return this.userProperties;
    }

    public UserType getUserType() {
        if (this.isLoggedIn()) {
            return this.userType == null ? UserType.LEGACY : this.userType;
        }

        return null;
    }

    protected void setUserType(UserType userType) {
        this.userType = userType;
    }

    protected void setUserId(String userId) {
        this.userId = userId;
    }

}
