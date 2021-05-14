package com.mojang.authlib.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

}
