package com.zyp.springai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyp.springai.entity.AiConfig;
import com.zyp.springai.mapper.AiConfigMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiConfigService extends ServiceImpl<AiConfigMapper, AiConfig> {

    /**
     * 内存缓存：configKey -> configValue
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 初始化时加载所有配置到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        configCache.clear();
        List<AiConfig> configs = list();
        for (AiConfig config : configs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
    }

    /**
     * 获取配置值（优先从缓存读取）
     */
    public String getValue(String key) {
        return configCache.getOrDefault(key, "");
    }

    /**
     * 获取配置值（带默认值）
     */
    public String getValue(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }

    /**
     * 按类型获取所有配置
     */
    public Map<String, String> getByType(String type) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : configCache.entrySet()) {
            AiConfig config = getOne(new LambdaQueryWrapper<AiConfig>()
                    .eq(AiConfig::getConfigKey, entry.getKey()));
            if (config != null && type.equals(config.getConfigType())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 获取所有配置（分类型）
     */
    public Map<String, List<AiConfig>> getAllGroupByType() {
        List<AiConfig> allConfigs = list();
        Map<String, List<AiConfig>> grouped = new HashMap<>();
        for (AiConfig config : allConfigs) {
            grouped.computeIfAbsent(config.getConfigType(), k -> new java.util.ArrayList<>()).add(config);
        }
        return grouped;
    }

    /**
     * 更新配置
     */
    public boolean updateConfig(String key, String value) {
        AiConfig config = getOne(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            boolean success = updateById(config);
            if (success) {
                configCache.put(key, value);
            }
            return success;
        }
        return false;
    }

    /**
     * 获取 AI 人设配置
     */
    public String getRole() {
        return getValue("ai.role", "全能AI助手");
    }

    public String getPersonality() {
        return getValue("ai.personality", "友好、专业、耐心");
    }

    public String getSpeakingStyle() {
        return getValue("ai.speaking_style", "简洁明了，通俗易懂");
    }

    public String getGreeting() {
        return getValue("ai.greeting", "你好！我是你的AI助手，有什么可以帮你的吗？");
    }

    /**
     * 获取系统提示词模板
     */
    public String getSystemPromptTemplate() {
        return getValue("ai.system_prompt_template",
                "你是一个{role}，具有以下特点：{personality}。\n你的说话风格是：{speaking_style}。\n\n{user_info}\n\n请根据以上信息，为用户提供个性化、有帮助的回答。");
    }
}
