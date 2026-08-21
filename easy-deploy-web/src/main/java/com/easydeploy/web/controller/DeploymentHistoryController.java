package com.easydeploy.web.controller;

import com.easydeploy.web.dto.ApiResponse;
import com.easydeploy.web.dto.response.DeploymentResponse;
import com.easydeploy.web.service.DeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deployments")
public class DeploymentHistoryController {

    private final DeploymentService deploymentService;

    public DeploymentHistoryController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeploymentResponse>>> getDeployments(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID userId) {
        List<DeploymentResponse> list;
        if (projectId != null) {
            list = deploymentService.getDeploymentsByProjectId(projectId);
        } else if (userId != null) {
            list = deploymentService.getDeploymentsByUserId(userId);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Either projectId or userId parameter is required"));
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeploymentResponse>> getDeploymentById(@PathVariable UUID id) {
        DeploymentResponse response = deploymentService.getDeploymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
