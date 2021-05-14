package com.mojang.authlib.yggdrasil.request;

import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class InvalidateRequest {

    private final String accessToken;
    private final String clientToken;

    public InvalidateRequest(YggdrasilUserAuthentication authenticationService) {
        this.accessToken = authenticationService.getAuthenticatedToken();
        this.clientToken = authenticationService.getAuthenticationService().getClientToken();
    }

}
