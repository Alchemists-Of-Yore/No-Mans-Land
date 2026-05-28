package com.farcr.nomansland.common.mixin.plugin.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(IfModPresent.List.class)
public @interface IfModPresent {
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        IfModPresent[] value();
    }
}
