package com.easydeploy.core.generator;

import com.easydeploy.core.model.ProjectConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TemplateRenderEngine {

    private final Configuration cfg;

    public TemplateRenderEngine() {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(TemplateRenderEngine.class, "/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    public String renderTemplate(String templatePath, ProjectConfig projectConfig) throws Exception {
        Template template = cfg.getTemplate(templatePath);
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("config", projectConfig);

        StringWriter out = new StringWriter();
        template.process(dataModel, out);
        return out.toString();
    }
}
