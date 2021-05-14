package com.mojang.authlib;

import com.mojang.authlib.minecraft.MinecraftSessionService;

public interface AuthenticationService {

    UserAuthentication createUserAuthentication(Agent agent);

    MinecraftSessionService createMinecraftSessionService();

    GameProfileRepository createProfileRepository();

}
