package com.github.cao.awa.sepals.transform.mixin.config.handler;

import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;
import com.google.gson.JsonObject;

public record SepalsMixinHandlerConfig(String group, String name, String className, SepalsMixinHandler handler) {
    public static SepalsMixinHandlerConfig create(JsonObject json, String className) {
        String group = json.get("group").getAsString();
        String name = json.get("name").getAsString();

        return new SepalsMixinHandlerConfig(group, name, className, SepalsMixinHandler.handler(group));
    }
}
