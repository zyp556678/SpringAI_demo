package com.zyp.springai.controller;

import com.zyp.springai.entity.AiConfig;
import com.zyp.springai.service.AiConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-config")
public class AiConfigController {

    private final AiConfigService aiConfigService;

    public AiConfigController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    /**
     * 获取所有配置（分类型）
     */
    @GetMapping
    public Map<String, Object> getAll() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", aiConfigService.getAllGroupByType());
        return result;
    }

    /**
     * 获取单个配置
     */
    @GetMapping("/{key}")
    public Map<String, Object> getByKey(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", aiConfigService.getValue(key));
        return result;
    }

    /**
     * 更新配置
     */
    @PutMapping("/{key}")
    public Map<String, Object> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        boolean success = aiConfigService.updateConfig(key, value);
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 404);
            result.put("message", "配置不存在");
        }
        return result;
    }

    /**
     * 批量更新配置
     */
    @PutMapping
    public Map<String, Object> batchUpdate(@RequestBody Map<String, String> configs) {
        int successCount = 0;
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            if (aiConfigService.updateConfig(entry.getKey(), entry.getValue())) {
                successCount++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", successCount);
        return result;
    }

    /**
     * 恢复默认配置
     */
    @PostMapping("/reset")
    public Map<String, Object> reset() {
        // 删除所有现有配置
        aiConfigService.removeByIds(aiConfigService.list().stream()
                .map(AiConfig::getId)
                .toList());

        // 重新插入默认配置（通过重新执行 SQL 脚本）
        // 这里我们直接返回提示，让用户重新执行 SQL
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "请重新执行 sql/init.sql 脚本以恢复默认配置");
        return result;
    }

    /**
     * 获取 AI 人设配置
     */
    @GetMapping("/personality")
    public Map<String, Object> getPersonality() {
        Map<String, Object> data = new HashMap<>();
        data.put("role", aiConfigService.getRole());
        data.put("personality", aiConfigService.getPersonality());
        data.put("speaking_style", aiConfigService.getSpeakingStyle());
        data.put("greeting", aiConfigService.getGreeting());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }
}
