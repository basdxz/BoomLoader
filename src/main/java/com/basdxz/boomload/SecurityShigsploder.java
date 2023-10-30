package com.basdxz.boomload;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.Permission;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SecurityShigsploder extends SecurityManager {
    private static boolean hasShigsploded = false;

    @SneakyThrows
    public static void shigsplodeSecurity() {
        if (hasShigsploded)
            return;

        val gdf0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        gdf0.setAccessible(true);
        val fields = (Field[]) gdf0.invoke(System.class, false);
        Field securityField = null;
        for (val field : fields) {
            if ("security".equals(field.getName())) {
                securityField = field;
                break;
            }
        }
        val unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        val theUnsafe = (Unsafe) unsafeField.get(null);

        val theStaticSystem = theUnsafe.staticFieldBase(securityField);
        val theSecurityOffset = theUnsafe.staticFieldOffset(securityField);
        theUnsafe.putObject(theStaticSystem, theSecurityOffset, null);
        System.setSecurityManager(new SecurityShigsploder());

        hasShigsploded = true;
    }

    @Override
    public void checkExit(int status) {throw new SecurityException();}

    @Override
    public void checkPermission(Permission perm) {}
}
