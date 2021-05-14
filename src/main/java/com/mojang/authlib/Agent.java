package com.mojang.authlib;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Agent {
    public static final Agent MINECRAFT = new Agent("Minecraft", 1);
    public static final Agent SCROLLS = new Agent("Scrolls", 1);

    private final String name;
    private final int version;

    public String toString() {
        return "Agent{name='" + this.name + '\'' + ", version=" + this.version + '}';
    }

}
