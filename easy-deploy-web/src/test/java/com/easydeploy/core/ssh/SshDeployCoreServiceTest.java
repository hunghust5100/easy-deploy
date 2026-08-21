package com.easydeploy.core.ssh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SshDeployCoreServiceTest {

    @Test
    public void testSshCredentialsDefaults() {
        SshDeployCoreService.SshCredentials creds = new SshDeployCoreService.SshCredentials();
        creds.setHost("103.179.188.1");
        creds.setUsername("admin");

        assertEquals("103.179.188.1", creds.getHost());
        assertEquals(22, creds.getPort());
        assertEquals("admin", creds.getUsername());
        assertEquals("/root/admin", creds.getDeployPath());
        assertTrue(creds.isRunSetupScript());
    }

    @Test
    public void testSshKeyCredentials() {
        SshDeployCoreService.SshCredentials creds = new SshDeployCoreService.SshCredentials("1.2.3.4", 2222, "ubuntu", null, "/var/www/app");
        creds.setKeyFilePath("~/.ssh/id_rsa");

        assertEquals("1.2.3.4", creds.getHost());
        assertEquals(2222, creds.getPort());
        assertEquals("ubuntu", creds.getUsername());
        assertEquals("~/.ssh/id_rsa", creds.getKeyFilePath());
        assertEquals("/var/www/app", creds.getDeployPath());
    }
}
