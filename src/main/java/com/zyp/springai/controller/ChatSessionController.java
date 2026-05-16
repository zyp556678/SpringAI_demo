package com.zyp.springai.controller;

import com.zyp.springai.entity.ChatSession;
import com.zyp.springai.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping
    public Map<String, Object> createSession(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String title = (String) params.get("title");

        ChatSession session = chatSessionService.createSession(userId, title);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", session);
        return result;
    }

    @GetMapping("/list/{userId}")
    public Map<String, Object> listSessions(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", chatSessionService.listByUserId(userId));
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getSession(@PathVariable Long id) {
        ChatSession session = chatSessionService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", session);
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateSession(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        ChatSession session = chatSessionService.getById(id);
        if (session != null) {
            session.setTitle((String) params.get("title"));
            chatSessionService.updateById(session);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", session);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteSession(@PathVariable Long id) {
        chatSessionService.removeById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
}
