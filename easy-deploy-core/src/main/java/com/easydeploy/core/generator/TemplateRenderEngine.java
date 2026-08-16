package com.easydeploy.core.generator;

import com.easydeploy.core.model.ProjectConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TemplateRenderEngine {

    private final Configuration cfg;
    private final ObjectMapper objectMapper;

    public TemplateRenderEngine() {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(TemplateRenderEngine.class, "/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        this.objectMapper = new ObjectMapper();
    }

    public String renderTemplate(String templatePath, ProjectConfig projectConfig) throws Exception {
        Template template = cfg.getTemplate(templatePath);
        
        // Expose both config object and its top-level properties to support any template syntax
        Map<String, Object> dataModel = new HashMap<>();
        if (projectConfig != null) {
            Map<String, Object> props = objectMapper.convertValue(projectConfig, new TypeReference<Map<String, Object>>() {});
            dataModel.putAll(props);
            dataModel.put("config", projectConfig);
        }

        StringWriter out = new StringWriter();
        template.process(dataModel, out);
        return out.toString();
    }
}
