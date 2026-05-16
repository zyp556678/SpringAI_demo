package com.zyp.springai.controller;

import com.zyp.springai.entity.ChatMessage;
import com.zyp.springai.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 普通对话（非流式）
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> params) {
        Long sessionId = Long.valueOf(params.get("sessionId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String message = (String) params.get("message");
        Long modelConfigId = params.containsKey("modelConfigId")
                ? Long.valueOf(params.get("modelConfigId").toString())
                : chatService.getDefaultModelConfigId();

        String reply = chatService.chat(sessionId, userId, message, modelConfigId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", reply);
        return result;
    }

    /**
     * 流式对话（SSE）
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestParam Long sessionId,
            @RequestParam Long userId,
            @RequestParam String message,
            @RequestParam(required = false) Long modelConfigId) {
        // 如果未指定模型，使用默认模型
        if (modelConfigId == null) {
            modelConfigId = chatService.getDefaultModelConfigId();
        }
        return chatService.chatStream(sessionId, userId, message, modelConfigId);
    }

    /**
     * 获取历史消息
     */
    @GetMapping("/messages/{sessionId}")
    public Map<String, Object> getMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = chatService.getHistory(sessionId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", messages);
        return result;
    }
}
