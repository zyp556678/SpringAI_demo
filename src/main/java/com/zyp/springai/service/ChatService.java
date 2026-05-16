package com.zyp.springai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyp.springai.config.DynamicChatClientFactory;
import com.zyp.springai.entity.AiModelConfig;
import com.zyp.springai.entity.ChatMessage;
import com.zyp.springai.entity.UserProfile;
import com.zyp.springai.mapper.ChatMessageMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    private final UserProfileService userProfileService;
    private final ChatSessionService chatSessionService;
    private final AiConfigService aiConfigService;
    private final AiModelConfigService aiModelConfigService;
    private final DynamicChatClientFactory chatClientFactory;

    public ChatService(UserProfileService userProfileService,
                       ChatSessionService chatSessionService,
                       AiConfigService aiConfigService,
                       AiModelConfigService aiModelConfigService,
                       DynamicChatClientFactory chatClientFactory) {
        this.userProfileService = userProfileService;
        this.chatSessionService = chatSessionService;
        this.aiConfigService = aiConfigService;
        this.aiModelConfigService = aiModelConfigService;
        this.chatClientFactory = chatClientFactory;
    }

    /**
     * 普通对话（非流式）
     */
    public String chat(Long sessionId, Long userId, String userMessage, Long modelConfigId) {
        // 保存用户消息
        saveMessage(sessionId, "user", userMessage);

        // 获取 ChatClient
        ChatClient chatClient = chatClientFactory.createClient(modelConfigId);

        // 构建消息列表
        List<Message> messages = buildMessages(sessionId, userId);

        // 调用 AI
        ChatResponse response = chatClient.prompt()
                .messages(messages)
                .user(userMessage)
                .call()
                .chatResponse();

        String assistantContent = response.getResult().getOutput().getText();

        // 保存助手回复
        saveMessage(sessionId, "assistant", assistantContent);

        return assistantContent;
    }

    /**
     * 流式对话（SSE）
     */
    public Flux<String> chatStream(Long sessionId, Long userId, String userMessage, Long modelConfigId) {
        // 保存用户消息
        saveMessage(sessionId, "user", userMessage);

        // 获取 ChatClient
        ChatClient chatClient = chatClientFactory.createClient(modelConfigId);

        // 构建消息列表
        List<Message> messages = buildMessages(sessionId, userId);

        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .messages(messages)
                .user(userMessage)
                .stream()
                .chatResponse()
                .handle((response, sink) -> {
                    String content = response.getResult().getOutput().getText();
                    if (content != null) {
                        fullResponse.append(content);
                        sink.next(content);
                    }
                })
                .cast(String.class)
                .doOnComplete(() -> {
                    // 流式完成后保存完整回复
                    if (fullResponse.length() > 0) {
                        saveMessage(sessionId, "assistant", fullResponse.toString());
                    }
                });
    }

    /**
     * 获取会话的历史消息
     */
    public List<ChatMessage> getHistory(Long sessionId) {
        return list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedAt));
    }

    /**
     * 构建消息列表（包含系统提示和历史消息）
     */
    private List<Message> buildMessages(Long sessionId, Long userId) {
        List<Message> messages = new ArrayList<>();

        // 1. 构建系统提示（注入用户个人信息和 AI 配置）
        String systemPrompt = buildSystemPrompt(userId);
        messages.add(new SystemMessage(systemPrompt));

        // 2. 加载历史消息
        List<ChatMessage> history = getHistory(sessionId);
        for (ChatMessage msg : history) {
            switch (msg.getRole()) {
                case "user" -> messages.add(new UserMessage(msg.getContent()));
                case "assistant" -> messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        return messages;
    }

    /**
     * 构建系统提示词（使用配置模板 + 用户个人信息）
     */
    private String buildSystemPrompt(Long userId) {
        // 1. 获取用户个人信息
        StringBuilder userInfo = new StringBuilder();
        UserProfile profile = userProfileService.getByUserId(userId);
        if (profile != null) {
            userInfo.append("## User Information\n");
            if (profile.getNickname() != null) {
                userInfo.append("- Name: ").append(profile.getNickname()).append("\n");
            }
            if (profile.getAge() != null) {
                userInfo.append("- Age: ").append(profile.getAge()).append("\n");
            }
            if (profile.getGender() != null) {
                userInfo.append("- Gender: ").append(profile.getGender()).append("\n");
            }
            if (profile.getOccupation() != null) {
                userInfo.append("- Occupation: ").append(profile.getOccupation()).append("\n");
            }
            if (profile.getInterests() != null) {
                userInfo.append("- Interests: ").append(profile.getInterests()).append("\n");
            }
            if (profile.getPersonality() != null) {
                userInfo.append("- Personality: ").append(profile.getPersonality()).append("\n");
            }
            if (profile.getExtraInfo() != null) {
                userInfo.append("- Other: ").append(profile.getExtraInfo()).append("\n");
            }
            userInfo.append("\nPlease consider the user's background, interests and needs when responding.");
        } else {
            userInfo.append("The user has not filled in personal information yet. Please provide general assistance.");
        }

        // 2. 获取 AI 配置
        String role = aiConfigService.getRole();
        String personality = aiConfigService.getPersonality();
        String speakingStyle = aiConfigService.getSpeakingStyle();
        String template = aiConfigService.getSystemPromptTemplate();

        // 3. 替换模板变量
        String systemPrompt = template
                .replace("{role}", role)
                .replace("{personality}", personality)
                .replace("{speaking_style}", speakingStyle)
                .replace("{user_info}", userInfo.toString());

        return systemPrompt;
    }

    /**
     * 获取 AI 开场白
     */
    public String getGreeting() {
        return aiConfigService.getGreeting();
    }

    /**
     * 获取默认模型配置ID
     */
    public Long getDefaultModelConfigId() {
        AiModelConfig defaultModel = aiModelConfigService.getDefaultModel();
        return defaultModel != null ? defaultModel.getId() : null;
    }

    private void saveMessage(Long sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        save(message);
    }
}
