package com.mojang.authlib.yggdrasil.request;

import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class AuthenticationRequest {

    private final Agent agent;
    private final String username;
    private final String password;
    private final String clientToken;
    private final boolean requestUser = true;

    public AuthenticationRequest(YggdrasilUserAuthentication authenticationService, String username, String password) {
        this.agent = authenticationService.getAgent();
        this.username = username;
        this.clientToken = authenticationService.getAuthenticationService().getClientToken();
        this.password = password;
    }

}
