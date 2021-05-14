package com.mojang.authlib.yggdrasil.response;

import lombok.Getter;

@Getter
public class Response {

    private String error;
    private String errorMessage;
    private String cause;

    protected void setError(String error) {
        this.error = error;
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected void setCause(String cause) {
        this.cause = cause;
    }

}
