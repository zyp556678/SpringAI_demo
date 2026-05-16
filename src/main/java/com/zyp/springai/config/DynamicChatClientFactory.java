package com.zyp.springai.config;

import com.zyp.springai.entity.AiModelConfig;
import com.zyp.springai.service.AiModelConfigService;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class DynamicChatClientFactory {

    private final AiModelConfigService modelConfigService;

    public DynamicChatClientFactory(AiModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 根据模型配置创建 ChatClient
     */
    public ChatClient createClient(Long modelConfigId) {
        AiModelConfig config = modelConfigService.getById(modelConfigId);
        if (config == null) {
            throw new IllegalArgumentException("Model config not found: " + modelConfigId);
        }
        return createClient(config);
    }

    /**
     * 根据模型配置创建 ChatClient
     */
    public ChatClient createClient(AiModelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Model config cannot be null");
        }

        return switch (config.getProvider()) {
            case "openai" -> createOpenAiCompatibleClient(config);
            case "anthropic" -> createAnthropicClient(config);
            default -> throw new IllegalArgumentException("Unknown provider: " + config.getProvider());
        };
    }

    /**
     * 创建 OpenAI 兼容客户端 (支持 Deepseek, OpenAI, 智谱, 通义, Moonshot 等)
     */
    private ChatClient createOpenAiCompatibleClient(AiModelConfig config) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModelId())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        OpenAiChatModel model = new OpenAiChatModel(api, options);
        return ChatClient.builder(model).build();
    }

    /**
     * 创建 Anthropic 客户端
     */
    private ChatClient createAnthropicClient(AiModelConfig config) {
        AnthropicApi api = new AnthropicApi(config.getApiKey());

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(config.getModelId())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        AnthropicChatModel model = new AnthropicChatModel(api, options);
        return ChatClient.builder(model).build();
    }
}
