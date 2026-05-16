package com.zyp.springai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyp.springai.entity.ChatSession;
import com.zyp.springai.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSession> {

    public List<ChatSession> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdatedAt));
    }

    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        save(session);
        return session;
    }
}
