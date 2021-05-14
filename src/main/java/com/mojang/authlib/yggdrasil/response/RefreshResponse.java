package com.mojang.authlib.yggdrasil.response;

import com.mojang.authlib.GameProfile;
import lombok.Getter;

@Getter
public class RefreshResponse extends Response {

    private String accessToken;
    private String clientToken;
    private GameProfile selectedProfile;
    private GameProfile[] availableProfiles;
    private User user;

}
