package com.basdxz.boomload.asm;

import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public final class Exclusions {
    public static final Set<String> EXCLUDED_PACKAGES = ImmutableSet.of(
            "org.lwjgl",
            "com.sun",
            "jdk",
            "javafx",
            "scala",
            "com.ibm",
            "io.netty",
            "akka",
            "java",
            "sun",
            "netscape",
            "com.jcraft",
            "org.apache",
            "tv.twitch",
            "paulscode",
            "org.multimc",
            "nonapi.io.github.classgraph",
            "net.java",
            "LZMA",
            "joptsimple",
            "javax",
            "ibxm",
            "gnu.trove",
            "com.google",
            "com.typesafe",
            "com.mojang");
    public static final Set<String> EXCLUDED_CLASSES = ImmutableSet.of();//"com.rwtema.extrautils.core.TestTransformer"
}
