package com.basdxz.boomload;

import com.falsepattern.lib.api.DependencyLoader;
import com.falsepattern.lib.api.SemanticVersion;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.ReflectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]",
        dependencies = "required-after:falsepatternlib")
public class BoomLoad {
    private static final Logger LOG = LogManager.getLogger(Tags.MODNAME);

    public static final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
    public static final List<Class> setOfAllClasses = new ArrayList<>();
    public static final List<Object> setOfAllObjects = new ArrayList<>();


    static {
        DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
        DependencyLoader.builder()
                .loadingModId(Tags.MODID)
                .groupId("io.github.classgraph")
                .artifactId("classgraph")
                .minVersion(new SemanticVersion(4, 8, 138))
                .maxVersion(new SemanticVersion(4, 8, Integer.MAX_VALUE))
                .preferredVersion(new SemanticVersion(4, 8, 138))
                .build();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        AtomicInteger failCount = new AtomicInteger();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("net.minecraft", "net.minecraftforge").rejectPackages("org.lwjgl").scan()) {
            scanResult.getAllClasses().forEach(c -> {
                if (c.isInnerClass())
                    return;

                String className = c.getName();
                try {
                    Class cls = Class.forName(c.loadClass(true).getCanonicalName());
                    setOfAllClasses.add(cls);
                    setOfAllObjects.add(ReflectionFactory.getReflectionFactory().newConstructorForSerialization(cls).newInstance());
                    LOG.info("Successfully loaded: " + className);
                } catch (Throwable t) {
                    failCount.getAndIncrement();
                    LOG.warn("Failed to load: " + className);
                }
            });
        }

        LOG.info("Loading complete!");
        LOG.info("Tried: " + (setOfAllClasses.size() + failCount.get()));
        LOG.info("Succeeded: " + setOfAllClasses.size());
        LOG.info("Failed: " + failCount);

        try {
            Class cls = Class.forName("net.minecraft.world.chunk.Chunk");
            cls.getDeclaredConstructor(null).newInstance();
        } catch (Throwable ignored) {
        }
    }
}