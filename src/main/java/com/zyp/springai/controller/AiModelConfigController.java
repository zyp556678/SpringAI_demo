package com.zyp.springai.controller;

import com.zyp.springai.entity.AiModelConfig;
import com.zyp.springai.service.AiModelConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-model-config")
public class AiModelConfigController {

    private final AiModelConfigService modelConfigService;

    public AiModelConfigController(AiModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 获取所有模型配置
     */
    @GetMapping
    public Map<String, Object> getAll() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", modelConfigService.list());
        return result;
    }

    /**
     * 获取启用的模型配置
     */
    @GetMapping("/enabled")
    public Map<String, Object> getEnabled() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", modelConfigService.getEnabledModels());
        return result;
    }

    /**
     * 获取单个模型配置
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", modelConfigService.getById(id));
        return result;
    }

    /**
     * 新增模型配置
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody AiModelConfig config) {
        boolean success = modelConfigService.save(config);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("code", 200);
            result.put("message", "Created successfully");
            result.put("data", config);
        } else {
            result.put("code", 500);
            result.put("message", "Create failed");
        }
        return result;
    }

    /**
     * 更新模型配置
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody AiModelConfig config) {
        config.setId(id);
        boolean success = modelConfigService.updateById(config);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("code", 200);
            result.put("message", "Updated successfully");
        } else {
            result.put("code", 500);
            result.put("message", "Update failed");
        }
        return result;
    }

    /**
     * 删除模型配置
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = modelConfigService.removeById(id);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("code", 200);
            result.put("message", "Deleted successfully");
        } else {
            result.put("code", 500);
            result.put("message", "Delete failed");
        }
        return result;
    }

    /**
     * 设置默认模型
     */
    @PostMapping("/{id}/set-default")
    public Map<String, Object> setDefault(@PathVariable Long id) {
        boolean success = modelConfigService.setDefault(id);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("code", 200);
            result.put("message", "Default model set successfully");
        } else {
            result.put("code", 500);
            result.put("message", "Failed to set default model");
        }
        return result;
    }

    /**
     * 测试模型连接
     */
    @PostMapping("/{id}/test")
    public Map<String, Object> testConnection(@PathVariable Long id) {
        boolean success = modelConfigService.testConnection(id);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("code", 200);
            result.put("message", "Connection test passed");
        } else {
            result.put("code", 400);
            result.put("message", "Connection test failed: please check API Key, Base URL and Model ID");
        }
        return result;
    }
}
