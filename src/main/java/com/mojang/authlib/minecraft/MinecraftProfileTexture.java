package com.mojang.authlib.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

@RequiredArgsConstructor
public class MinecraftProfileTexture {

    @Getter private final String url;
    private final Map<String, String> metadata;

    public String getMetadata(String key) {
        return this.metadata == null ? null : this.metadata.get(key);
    }

    public String getHash() {
        return FilenameUtils.getBaseName(this.url);
    }

    public String toString() {
        return new ToStringBuilder(this).append("url", this.url).append("hash", this.getHash()).toString();
    }

    public enum Type {
        SKIN,
        CAPE
    }

}
