package com.basdxz.boomload;


import com.basdxz.boomload.asm.Exclusions;
import com.basdxz.boomload.asm.OpenGLFunctionDumper;
import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import io.github.classgraph.ClassGraph;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.basdxz.boomload.SecurityShigsploder.shigsplodeSecurity;
import static com.basdxz.boomload.Tags.*;

@Mod(modid = MOD_ID,
     version = VERSION,
     name = MOD_NAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib;after:*")
public final class BoomLoader {
    private static final Logger LOG = LogManager.getLogger(MOD_NAME);

    public static final Set<Class<?>> LOADED_CLASSES = new HashSet<>();

    static {
        DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
        DependencyLoader.loadLibraries(
                Library.builder()
                       .loadingModId(MOD_ID)
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
        shigsplodeSecurity();

        val mappings = notchToSRG();
        val failCount = new AtomicInteger();

        @Cleanup val scanResult = new ClassGraph()
                .enableAllInfo()
                .rejectPackages(Exclusions.EXCLUDED_PACKAGES.toArray(new String[0])).scan();
        scanResult.getAllClasses().forEach(c -> {
            var className = c.getName();
            if (Exclusions.EXCLUDED_CLASSES.contains(className))
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
                LOADED_CLASSES.add(Class.forName(className));
                LOG.info("Successfully loaded: " + className);
            } catch (Throwable t) {
                failCount.getAndIncrement();
                LOG.warn("Failed to load: " + className);
            }
        });

        LOG.info("Loading complete!");
        LOG.info("Tried: " + (LOADED_CLASSES.size() + failCount.get()));
        LOG.info("Succeeded: " + LOADED_CLASSES.size());
        LOG.info("Failed: " + failCount);

//        try (ScanResult scanResult2 = new ClassGraph().scan()) {
//            scanResult.getResourcesWithExtension("png").
//                      forEachByteArrayThrowingIOException((Resource res, byte[] fileContent) -> {
//                          val bis = new ByteArrayInputStream(fileContent);
//                          val bImage2 = ImageIO.read(bis);
//                          val imageFile = new File("imgdump", res.getPath());
//                          new File(imageFile.getParentFile().getAbsolutePath()).mkdirs();
//                          ImageIO.write(bImage2, "png", imageFile);
//                          LOG.info("Dumped image: " + res.getPath());
//                      });
//        }

        iSleep();
    }

    @SneakyThrows
    private void iSleep() {
        OpenGLFunctionDumper.dumpOpenGLFunctions();

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
