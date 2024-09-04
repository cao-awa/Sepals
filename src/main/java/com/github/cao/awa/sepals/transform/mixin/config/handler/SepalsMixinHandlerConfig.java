package com.github.cao.awa.sepals.transform.mixin.config.handler;

import com.alibaba.fastjson2.JSONObject;
import com.github.cao.awa.sepals.transform.mixin.handler.SepalsMixinHandler;

public record SepalsMixinHandlerConfig(String group, String name, String className, SepalsMixinHandler handler) {
    public static SepalsMixinHandlerConfig create(JSONObject json, String className) {
        String group = json.getString("group");
        String name = json.getString("name");

        return new SepalsMixinHandlerConfig(group, name, className, SepalsMixinHandler.handler(group));
    }
}
