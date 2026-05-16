package com.zyp.springai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyp.springai.entity.UserProfile;
import com.zyp.springai.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService extends ServiceImpl<UserProfileMapper, UserProfile> {

    public UserProfile getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getId, userId));
    }
}
