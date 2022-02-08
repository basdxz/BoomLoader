package com.basdxz.boomload;

import java.security.Permission;

public class ExplosiveManager extends SecurityManager {
    @Override
    public void checkExit(int status) {
        throw new SecurityException();
    }

    @Override
    public void checkPermission(Permission perm) {
    }
}
