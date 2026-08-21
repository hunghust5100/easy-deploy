package com.easydeploy.web;

import com.easydeploy.web.dto.ApiResponse;
import com.easydeploy.web.dto.request.ProjectRequest;
import com.easydeploy.web.dto.request.ServerRequest;
import com.easydeploy.web.dto.request.UserRequest;
import com.easydeploy.web.dto.response.ProjectResponse;
import com.easydeploy.web.dto.response.ServerResponse;
import com.easydeploy.web.dto.response.UserResponse;
import com.easydeploy.web.service.DeploymentService;
import com.easydeploy.web.service.ProjectService;
import com.easydeploy.web.service.ServerService;
import com.easydeploy.web.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class EasyDeployCrudIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DeploymentService deploymentService;

    @Test
    void testFullCrudLifecycle() {
        // 1. Create User
        UserRequest userReq = new UserRequest();
        userReq.setEmail("hung.test@sis.hust.edu.vn");
        userReq.setFullName("Nguyen Khanh Hung");
        userReq.setPassword("secret123");
        UserResponse createdUser = userService.createUser(userReq);

        assertNotNull(createdUser.getId());
        assertEquals("hung.test@sis.hust.edu.vn", createdUser.getEmail());
        assertEquals("DEVELOPER", createdUser.getRole());

        // 2. Query User
        UserResponse fetchedUser = userService.getUserById(createdUser.getId());
        assertEquals("Nguyen Khanh Hung", fetchedUser.getFullName());

        // 3. Create Server for User
        ServerRequest serverReq = new ServerRequest();
        serverReq.setUserId(createdUser.getId());
        serverReq.setName("Production Server 1");
        serverReq.setHost("103.200.23.45");
        serverReq.setSshPort(22);
        serverReq.setSshUser("root");
        serverReq.setPassword("vps_password_123");
        serverReq.setDefaultDeployPath("/var/www/apps");
        ServerResponse createdServer = serverService.createServer(serverReq);

        assertNotNull(createdServer.getId());
        assertEquals("Production Server 1", createdServer.getName());
        assertEquals("103.200.23.45", createdServer.getHost());

        // 4. Query Server by User
        List<ServerResponse> servers = serverService.getServersByUserId(createdUser.getId());
        assertEquals(1, servers.size());

        // 5. Create Project for User linked to Server
        ProjectRequest projectReq = new ProjectRequest();
        projectReq.setUserId(createdUser.getId());
        projectReq.setServerId(createdServer.getId());
        projectReq.setAppName("easy-deploy-demo");
        projectReq.setRepoUrl("https://github.com/hunghust5100/easy-deploy.git");
        projectReq.setTechStack("JAVA_GRADLE");
        projectReq.setTechVersion("21");
        projectReq.setAppPort(8088);
        projectReq.setHostPort(80);
        projectReq.setDbType("POSTGRESQL");
        projectReq.setDbName("easy_deploy_db");
        projectReq.setDbUser("postgres");
        projectReq.setDbPass("dbsecret");
        projectReq.setEnableNginx(true);
        projectReq.setDomainName("easydeploy.example.com");
        projectReq.setEnvVars(Map.of("SPRING_PROFILES_ACTIVE", "prod", "JWT_SECRET", "my_super_secret_jwt_key"));

        ProjectResponse createdProject = projectService.createProject(projectReq);
        assertNotNull(createdProject.getId());
        assertEquals("easy-deploy-demo", createdProject.getAppName());
        assertEquals("JAVA_GRADLE", createdProject.getTechStack());
        assertEquals(2, createdProject.getEnvVars().size());

        // 6. Query Projects by User
        List<ProjectResponse> userProjects = projectService.getProjectsByUserId(createdUser.getId());
        assertEquals(1, userProjects.size());
        assertEquals("Production Server 1", userProjects.get(0).getServerName());

        // 7. Update Project
        projectReq.setAppPort(9090);
        // 8. Delete Project
        projectService.deleteProject(createdProject.getId());
        List<ProjectResponse> remainingProjects = projectService.getProjectsByUserId(createdUser.getId());
        assertEquals(0, remainingProjects.size());
    }

    @Autowired
    private com.easydeploy.web.service.AuthService authService;

    @Test
    void testAuthServiceLifecycle() {
        // 1. Register
        com.easydeploy.web.dto.request.RegisterRequest regReq = new com.easydeploy.web.dto.request.RegisterRequest(
                "Nguyen Khanh Hung",
                "hung.auth.test@sis.hust.edu.vn",
                "password123"
        );
        UserResponse registered = authService.register(regReq);
        assertNotNull(registered.getId());
        assertEquals("hung.auth.test@sis.hust.edu.vn", registered.getEmail());

        // 2. Login Success
        com.easydeploy.web.dto.request.LoginRequest loginReq = new com.easydeploy.web.dto.request.LoginRequest(
                "hung.auth.test@sis.hust.edu.vn",
                "password123"
        );
        UserResponse loggedIn = authService.login(loginReq);
        assertEquals(registered.getId(), loggedIn.getId());

        // 3. Login Wrong Password Failure
        com.easydeploy.web.dto.request.LoginRequest wrongPass = new com.easydeploy.web.dto.request.LoginRequest(
                "hung.auth.test@sis.hust.edu.vn",
                "wrongpass"
        );
        assertThrows(IllegalArgumentException.class, () -> authService.login(wrongPass));

        // 4. Duplicate Register Failure
        assertThrows(IllegalArgumentException.class, () -> authService.register(regReq));
    }

    @Autowired
    private com.easydeploy.web.controller.EnumController enumController;

    @Test
    void testEnumEndpoint() {
        var response = enumController.getAllEnums();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        var body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        var data = body.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("techStacks"));
        assertTrue(data.containsKey("dbTypes"));
        assertTrue(data.containsKey("deployModes"));

        var techStacks = (java.util.List<?>) data.get("techStacks");
        assertEquals(11, techStacks.size(), "Must have exactly 11 tech stacks");

        var dbTypes = (java.util.List<?>) data.get("dbTypes");
        assertEquals(6, dbTypes.size(), "Must have exactly 6 db types");

        var deployModes = (java.util.List<?>) data.get("deployModes");
        assertEquals(2, deployModes.size(), "Must have exactly 2 deploy modes");
    }

    @Autowired
    private com.easydeploy.web.controller.DeploymentHistoryController deploymentHistoryController;

    @Autowired
    private com.easydeploy.web.repository.DeploymentRepository deploymentRepository;

    @Test
    void testDeploymentHistoryLifecycle() {
        // 1. Create User
        UserRequest userReq = new UserRequest();
        userReq.setEmail("deploy.history.test@sis.hust.edu.vn");
        userReq.setFullName("Deploy Tester");
        userReq.setPassword("pass123");
        UserResponse user = userService.createUser(userReq);

        // 2. Create Project
        ProjectRequest projectReq = new ProjectRequest();
        projectReq.setUserId(user.getId());
        projectReq.setAppName("deploy-history-app");
        projectReq.setTechStack("PYTHON");
        ProjectResponse project = projectService.createProject(projectReq);

        // 3. Create Deployment record
        com.easydeploy.web.entity.DeploymentEntity entity = new com.easydeploy.web.entity.DeploymentEntity();
        entity.setUser(userService.getUserEntityById(user.getId()));
        entity.setProject(projectService.getProjectEntityById(project.getId()));
        entity.setTriggerSource("WEB_UI");
        entity.setStatus("RUNNING");
        entity.setStartedAt(java.time.LocalDateTime.now());
        entity = deploymentRepository.save(entity);

        assertNotNull(entity.getId());

        // 4. Update Deployment status
        deploymentService.updateDeploymentStatus(entity.getId(), "SUCCESS", "Build success\nContainers started", entity.getStartedAt());

        // 5. Query Deployments by User
        var userDeployments = deploymentService.getDeploymentsByUserId(user.getId());
        assertEquals(1, userDeployments.size());
        assertEquals("SUCCESS", userDeployments.get(0).getStatus());
        assertEquals("deploy-history-app", userDeployments.get(0).getAppName());

        // 6. Query Deployments by Project
        var projectDeployments = deploymentService.getDeploymentsByProjectId(project.getId());
        assertEquals(1, projectDeployments.size());

        // 7. Query Deployment Detail with Full Logs
        var detail = deploymentService.getDeploymentById(entity.getId());
        assertNotNull(detail.getLogContent());
        assertTrue(detail.getLogContent().contains("Containers started"));

        // 8. Controller endpoint test
        var controllerRes = deploymentHistoryController.getDeployments(project.getId(), null);
        assertNotNull(controllerRes.getBody());
        assertTrue(controllerRes.getBody().isSuccess());
        assertEquals(1, controllerRes.getBody().getData().size());
    }
}
