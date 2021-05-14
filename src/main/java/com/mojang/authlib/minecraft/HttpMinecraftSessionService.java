package com.mojang.authlib.minecraft;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.BaseMinecraftSessionService;

public abstract class HttpMinecraftSessionService
        extends BaseMinecraftSessionService {
    protected HttpMinecraftSessionService(HttpAuthenticationService authenticationService) {
        super((AuthenticationService)((Object)authenticationService));
    }

    public HttpAuthenticationService getAuthenticationService() {
        return (HttpAuthenticationService)((Object)super.getAuthenticationService());
    }
}
