package PsychologicalCounselingPlatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import PsychologicalCounselingPlatform.dto.LoginRequest;
import PsychologicalCounselingPlatform.dto.LoginResponse;
import PsychologicalCounselingPlatform.entity.User;
import PsychologicalCounselingPlatform.mapper.UserMapper;
import PsychologicalCounselingPlatform.service.UserService;
import PsychologicalCounselingPlatform.util.JwtUtil;
import PsychologicalCounselingPlatform.util.SecurityUtil;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getUserByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 清除该用户的旧token
        Set<String> keys = redisTemplate.keys("token:*");
        if (keys != null) {
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (request.getUsername().equals(value)) {
                    redisTemplate.delete(key);
                }
            }
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        
        // 将token存入Redis，设置过期时间
        redisTemplate.opsForValue().set("token:" + token, user.getUsername(), 24, TimeUnit.HOURS);
        
        return response;
    }
    
    @Override
    public User register(LoginRequest request) {
        if (getUserByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public void logout(String token) {
        redisTemplate.delete("token:" + token);
    }
    
    @Override
    public void resetPassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }
    
    @Override
    public User getProfile() {
        String userId = SecurityUtil.getCurrentUserId();
        return userMapper.selectById(userId);
    }
    
    @Override
    public User updateBasicInfo(User user) {
        String userId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(userId);
        
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 只更新基本信息字段
        currentUser.setUsername(user.getUsername());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhone(user.getPhone());
        currentUser.setQq(user.getQq());
        currentUser.setUpdateTime(LocalDateTime.now());
        
        userMapper.updateById(currentUser);
        return currentUser;
    }
    
    @Override
    public boolean verifyPassword(String password) {
        String userId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(userId);
        
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return passwordEncoder.matches(password, currentUser.getPassword());
    }
    
    @Override
    public void updatePassword(String currentPassword, String newPassword) {
        String userId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(userId);
        
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 验证新密码与当前密码不同
        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }
        
        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setUpdateTime(LocalDateTime.now());
        
        userMapper.updateById(currentUser);
    }
} 