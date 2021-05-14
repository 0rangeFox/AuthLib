package com.mojang.authlib.yggdrasil.response;

import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MinecraftProfilePropertiesResponse extends Response {

    private UUID id;
    private String name;
    private PropertyMap properties;

}
