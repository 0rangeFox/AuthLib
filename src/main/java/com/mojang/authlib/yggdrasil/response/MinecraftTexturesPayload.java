package com.mojang.authlib.yggdrasil.response;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class MinecraftTexturesPayload {

    private long timestamp;
    private UUID profileId;
    private String profileName;
    private boolean isPublic;
    private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;

}
