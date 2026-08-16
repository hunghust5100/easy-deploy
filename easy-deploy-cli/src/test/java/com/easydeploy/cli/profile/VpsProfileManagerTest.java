package com.easydeploy.cli.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class VpsProfileManagerTest {

    @Test
    public void testProfileModel() {
        VpsProfileManager.VpsProfile profile = new VpsProfileManager.VpsProfile(
            "prod", "103.179.1.1", 22, "root", "secret123", "~/.ssh/id_rsa", "/root/myapp"
        );

        assertEquals("prod", profile.getName());
        assertEquals("103.179.1.1", profile.getHost());
        assertEquals(22, profile.getPort());
        assertEquals("root", profile.getUsername());
        assertEquals("secret123", profile.getPassword());
        assertEquals("~/.ssh/id_rsa", profile.getKeyFilePath());
        assertEquals("/root/myapp", profile.getDeployPath());
    }
}
