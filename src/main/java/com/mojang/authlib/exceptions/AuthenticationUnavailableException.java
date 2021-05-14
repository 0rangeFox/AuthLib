package com.mojang.authlib.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthenticationUnavailableException extends AuthenticationException {

    public AuthenticationUnavailableException(String message) {
        super(message);
    }

    public AuthenticationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationUnavailableException(Throwable cause) {
        super(cause);
    }

}
