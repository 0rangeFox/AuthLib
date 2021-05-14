package com.mojang.authlib.yggdrasil.response;

import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;

@Getter
public class User {

    private String id;
    private PropertyMap properties;

}
