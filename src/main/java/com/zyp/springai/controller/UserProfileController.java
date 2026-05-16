package com.zyp.springai.controller;

import com.zyp.springai.entity.UserProfile;
import com.zyp.springai.service.UserProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/profile")
    public Map<String, Object> saveProfile(@RequestBody UserProfile profile) {
        userProfileService.saveOrUpdate(profile);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "保存成功");
        result.put("data", profile);
        return result;
    }

    @GetMapping("/profile/{id}")
    public Map<String, Object> getProfile(@PathVariable Long id) {
        UserProfile profile = userProfileService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", profile);
        return result;
    }

    @GetMapping("/profiles")
    public Map<String, Object> listProfiles() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", userProfileService.list());
        return result;
    }

    @DeleteMapping("/profile/{id}")
    public Map<String, Object> deleteProfile(@PathVariable Long id) {
        userProfileService.removeById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
}
