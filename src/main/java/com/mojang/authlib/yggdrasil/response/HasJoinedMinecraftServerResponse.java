package com.mojang.authlib.yggdrasil.response;

import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;

import java.util.UUID;

@Getter
public class HasJoinedMinecraftServerResponse extends Response {

    private UUID id;
    private PropertyMap properties;

}
