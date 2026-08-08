package com.easydeploy.web.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DeploymentServiceTest {

    @Test
    void testSshCredentialsDefaults() {
        DeploymentService.SshCredentials creds = new DeploymentService.SshCredentials();
        creds.setHost("192.168.1.100");
        creds.setUsername("root");

        assertEquals("192.168.1.100", creds.getHost());
        assertEquals(22, creds.getPort());
        assertEquals("root", creds.getUsername());
        assertEquals("/root/root", creds.getDeployPath());
    }

    @Test
    void testJSchInitialization() throws Exception {
        JSch jsch = new JSch();
        assertNotNull(jsch);

        // Test creating JSch session configuration without actual network connect
        Session session = jsch.getSession("testuser", "127.0.0.1", 22);
        session.setConfig("StrictHostKeyChecking", "no");

        assertEquals("testuser", session.getUserName());
        assertEquals("127.0.0.1", session.getHost());
        assertEquals(22, session.getPort());
    }
}
