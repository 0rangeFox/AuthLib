package com.mojang.authlib.yggdrasil.request;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class JoinMinecraftServerRequest {

    public String accessToken;
    public UUID selectedProfile;
    public String serverId;

    public JoinMinecraftServerRequest(GameProfile profile, String authenticationToken, String serverId) {
        this.selectedProfile = profile.getId();
        this.accessToken = authenticationToken;
        this.serverId = serverId;
    }

}
