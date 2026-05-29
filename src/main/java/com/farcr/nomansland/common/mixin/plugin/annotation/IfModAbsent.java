package com.farcr.nomansland.common.mixin.plugin.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(IfModAbsent.List.class)
public @interface IfModAbsent {
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        IfModAbsent[] value();
    }
}
