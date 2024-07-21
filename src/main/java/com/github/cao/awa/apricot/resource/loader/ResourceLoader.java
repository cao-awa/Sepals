package com.github.cao.awa.apricot.resource.loader;

import com.github.cao.awa.apricot.annotations.Stable;

import java.io.InputStream;
import java.net.URL;

@Stable
public class ResourceLoader {
    public static InputStream stream(String resource) {
        return ResourceLoader.class.getClassLoader()
                                   .getResourceAsStream(resource);
    }

    public static URL url(String resource) {
        return ResourceLoader.class.getClassLoader()
                                   .getResource(resource);
    }
}

