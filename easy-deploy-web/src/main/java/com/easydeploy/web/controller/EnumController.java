package com.easydeploy.web.controller;

import com.easydeploy.core.model.enums.DbType;
import com.easydeploy.core.model.enums.DeployMode;
import com.easydeploy.core.model.enums.TechStack;
import com.easydeploy.web.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/enums")
public class EnumController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllEnums() {
        Map<String, Object> data = new HashMap<>();

        List<Map<String, Object>> techStacks = Arrays.stream(TechStack.values())
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", t.name());
                    map.put("label", t.getDisplayName());
                    map.put("defaultVersion", t.getDefaultVersion());
                    map.put("defaultPort", t.getDefaultPort());
                    return map;
                })
                .toList();

        List<Map<String, Object>> dbTypes = Arrays.stream(DbType.values())
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", d.name());
                    map.put("label", d.getDisplayName());
                    map.put("defaultPort", d.getDefaultPort());
                    return map;
                })
                .toList();

        List<Map<String, Object>> deployModes = Arrays.stream(DeployMode.values())
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", m.name());
                    map.put("label", m.getDisplayName());
                    map.put("desc", m.getDescription());
                    return map;
                })
                .toList();

        data.put("techStacks", techStacks);
        data.put("dbTypes", dbTypes);
        data.put("deployModes", deployModes);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
