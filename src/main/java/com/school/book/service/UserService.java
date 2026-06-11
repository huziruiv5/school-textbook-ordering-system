package com.school.book.service;

import com.school.book.entity.User;
import com.school.book.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 登录验证
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 简单密码验证（课设演示，生产环境应使用BCrypt）
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据角色获取用户
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
