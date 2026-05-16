package com.zyp.springai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyp.springai.entity.AiModelConfig;
import com.zyp.springai.mapper.AiModelConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiModelConfigService extends ServiceImpl<AiModelConfigMapper, AiModelConfig> {

    /**
     * 获取所有启用的模型配置
     */
    public List<AiModelConfig> getEnabledModels() {
        return list(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getIsEnabled, true)
                .orderByDesc(AiModelConfig::getIsDefault)
                .orderByAsc(AiModelConfig::getModelName));
    }

    /**
     * 获取默认模型配置
     */
    public AiModelConfig getDefaultModel() {
        return getOne(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getIsDefault, true)
                .eq(AiModelConfig::getIsEnabled, true));
    }

    /**
     * 设置默认模型
     */
    public boolean setDefault(Long id) {
        // 先取消所有默认
        List<AiModelConfig> all = list();
        for (AiModelConfig config : all) {
            if (config.getIsDefault()) {
                config.setIsDefault(false);
                updateById(config);
            }
        }
        // 设置新的默认
        AiModelConfig target = getById(id);
        if (target != null) {
            target.setIsDefault(true);
            target.setIsEnabled(true);
            return updateById(target);
        }
        return false;
    }

    /**
     * 测试模型连接
     */
    public boolean testConnection(Long id) {
        // 简单验证配置是否存在且完整
        AiModelConfig config = getById(id);
        if (config == null) return false;
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) return false;
        if (config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) return false;
        if (config.getModelId() == null || config.getModelId().isEmpty()) return false;
        return true;
    }
}
