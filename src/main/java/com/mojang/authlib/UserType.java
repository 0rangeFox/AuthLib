package com.mojang.authlib;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum UserType {
    LEGACY("legacy"),
    MOJANG("mojang");

    private static final Map<String, UserType> BY_NAME;

    static {
        BY_NAME = new HashMap<String, UserType>();

        for (UserType type : UserType.values()) {
            BY_NAME.put(type.name, type);
        }
    }

    private final String name;

    public static UserType byName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

}
