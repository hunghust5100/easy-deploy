package com.easydeploy.web.controller;

import com.easydeploy.web.dto.ApiResponse;
import com.easydeploy.web.dto.request.ServerRequest;
import com.easydeploy.web.dto.response.ServerResponse;
import com.easydeploy.web.service.ServerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServerResponse>> createServer(@Valid @RequestBody ServerRequest request) {
        ServerResponse response = serverService.createServer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Server added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServerResponse>>> getServersByUser(@RequestParam UUID userId) {
        List<ServerResponse> list = serverService.getServersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerResponse>> getServerById(@PathVariable UUID id) {
        ServerResponse response = serverService.getServerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerResponse>> updateServer(@PathVariable UUID id, @RequestBody ServerRequest request) {
        ServerResponse updated = serverService.updateServer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Server updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteServer(@PathVariable UUID id) {
        serverService.deleteServer(id);
        return ResponseEntity.ok(ApiResponse.success("Server deleted successfully", null));
    }

    @PostMapping("/{id}/test-connection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection(@PathVariable UUID id) {
        boolean reachable = serverService.testConnection(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "connected", reachable,
                "message", reachable ? "SSH Connection established successfully" : "SSH Connection failed"
        )));
    }
}
