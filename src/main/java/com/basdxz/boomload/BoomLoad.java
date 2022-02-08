package com.basdxz.boomload;

import com.falsepattern.lib.api.DependencyLoader;
import com.falsepattern.lib.api.SemanticVersion;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]",
        dependencies = "required-after:falsepatternlib")
public class BoomLoad {
    public static final Set<Class> setOfAllClasses = new HashSet<>();

    private static Logger LOG = LogManager.getLogger(Tags.MODID);

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
        DependencyLoader.builder()
                .loadingModId(Tags.MODID)
                .groupId("io.github.classgraph")
                .artifactId("classgraph")
                .minVersion(new SemanticVersion(4, 8, 138))
                .maxVersion(new SemanticVersion(4, 8, Integer.MAX_VALUE))
                .preferredVersion(new SemanticVersion(4, 8, 138))
                .build();

        AtomicInteger failCount = new AtomicInteger();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().rejectPackages("org.lwjgl").scan()) {
            scanResult.getAllClasses().forEach(c -> {
                String className = c.getName();
                try {
                    setOfAllClasses.add(Class.forName(c.loadClass(true).getCanonicalName()));
                    LOG.info("Successfully loaded: " + className);
                } catch (Throwable t) {
                    failCount.getAndIncrement();
                    LOG.warn("Failed to load: " + className);
                }
            });
        }

        LOG.info("Loading complete!");
        LOG.info("Tried: " + setOfAllClasses.size() + failCount);
        LOG.info("Succeeded: " + setOfAllClasses.size());
        LOG.info("Failed: " + failCount);
    }
}