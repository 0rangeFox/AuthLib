package com.mojang.authlib.yggdrasil;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProfileIncompleteException extends RuntimeException {

    public ProfileIncompleteException(String message) {
        super(message);
    }

    public ProfileIncompleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileIncompleteException(Throwable cause) {
        super(cause);
    }

}
