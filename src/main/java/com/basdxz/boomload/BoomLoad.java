package com.basdxz.boomload;


import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import sun.misc.Unsafe;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib;after:*")
public class BoomLoad {
    private static final Logger LOG = LogManager.getLogger(Tags.MODNAME);

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
            "com.mojang"
                                                                       );
    public static final Set<String> EXCLUDED_CLASSES = ImmutableSet.of();//"com.rwtema.extrautils.core.TestTransformer"
    public static final Set<Class> loadedClasses = new HashSet<>();

    static {
        DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
        DependencyLoader.loadLibraries(
                Library.builder()
                       .loadingModId(Tags.MODID)
                       .groupId("io.github.classgraph")
                       .artifactId("classgraph")
                       .minVersion(new SemanticVersion(4, 8, 163))
                       .maxVersion(new SemanticVersion(4, 8, Integer.MAX_VALUE))
                       .preferredVersion(new SemanticVersion(4, 8, 163))
                       .build());
        ;
    }

    @Mod.EventHandler
    @SneakyThrows
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
        cheekySecurity();

        val mappings = notchToSRG();
        val failCount = new AtomicInteger();

        @Cleanup val scanResult = new ClassGraph()
                .enableAllInfo()
                .rejectPackages(EXCLUDED_PACKAGES.toArray(new String[0])).scan();
        scanResult.getAllClasses().forEach(c -> {
            var className = c.getName();
            if (EXCLUDED_CLASSES.contains(className))
                return;

            if (className.contains("$")) {
                val index = className.indexOf("$");
                val prefix = className.substring(0, index);
                if (mappings.containsKey(prefix)) {
                    className = mappings.get(prefix) + className.substring(index);
                }
            } else if (mappings.containsKey(className)) {
                className = mappings.get(className);
            }

            try {
                loadedClasses.add(Class.forName(className));
                LOG.info("Successfully loaded: " + className);
            } catch (Throwable t) {
                failCount.getAndIncrement();
                LOG.warn("Failed to load: " + className);
            }
        });

        LOG.info("Loading complete!");
        LOG.info("Tried: " + (loadedClasses.size() + failCount.get()));
        LOG.info("Succeeded: " + loadedClasses.size());
        LOG.info("Failed: " + failCount);

        try (ScanResult scanResult2 = new ClassGraph().scan()) {
            scanResult.getResourcesWithExtension("png").
                      forEachByteArrayThrowingIOException((Resource res, byte[] fileContent) -> {
                          val bis = new ByteArrayInputStream(fileContent);
                          val bImage2 = ImageIO.read(bis);
                          val imageFile = new File("imgdump", res.getPath());
                          new File(imageFile.getParentFile().getAbsolutePath()).mkdirs();
                          ImageIO.write(bImage2, "png", imageFile);
                          LOG.info("Dumped image: " + res.getPath());
                      });
        }

        iSleep();
    }

    @SneakyThrows
    private void cheekySecurity() {
        Method gdf0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        gdf0.setAccessible(true);
        Field[] fields = (Field[]) gdf0.invoke(System.class, false);
        Field securityField = null;
        for (val field : fields) {
            if (field.getName().equals("security")) {
                securityField = field;
                break;
            }
        }
        Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
        unsafe.setAccessible(true);
        Unsafe theUnsafe = (Unsafe) unsafe.get(null);
        Object theStaticSystem = theUnsafe.staticFieldBase(securityField);
        long theSecurityOffset = theUnsafe.staticFieldOffset(securityField);
        theUnsafe.putObject(theStaticSystem, theSecurityOffset, null);
        System.setSecurityManager(new ExplosiveManager());
    }

    @SneakyThrows
    private void iSleep() {
        LOG.info("And now, iSleep.");
        Thread.sleep(100000000);
    }

    @SneakyThrows
    private Map<String, String> notchToSRG() {
        val notchToSRG = new HashMap<String, String>();
        val in = getClass().getResourceAsStream("/notch-srg.srg");
        val data = new ByteArrayOutputStream();
        int theByte;
        while ((theByte = in.read()) != -1) {
            data.write(theByte);
        }
        in.close();

        val lines = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(data.toByteArray())).toString().split("\n");

        Arrays.asList(lines).forEach(line -> {
            if (!line.startsWith("CL: ")) return;
            String[] parts = line.split(" ");
            notchToSRG.put(parts[1], parts[2].replace('/', '.'));
        });
        return notchToSRG;
    }
}
