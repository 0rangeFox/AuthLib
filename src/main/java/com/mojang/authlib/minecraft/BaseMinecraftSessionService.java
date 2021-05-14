package com.mojang.authlib.minecraft;

import com.mojang.authlib.AuthenticationService;
import lombok.Getter;

public abstract class BaseMinecraftSessionService implements MinecraftSessionService {

    @Getter
    private final AuthenticationService authenticationService;

    protected BaseMinecraftSessionService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

}
