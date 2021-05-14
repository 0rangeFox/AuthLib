package com.mojang.authlib.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;

public interface MinecraftSessionService {
    public void joinServer(GameProfile var1, String var2, String var3) throws AuthenticationException;

    public GameProfile hasJoinedServer(GameProfile var1, String var2) throws AuthenticationUnavailableException;

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile var1, boolean var2);

    public GameProfile fillProfileProperties(GameProfile var1, boolean var2);
}
