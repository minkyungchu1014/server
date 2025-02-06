package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long userIdSequence = 1L; // 사용자 ID 자동 증가 시퀀스

    /**
     * 사용자 저장 (ID 자동 증가)
     */
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(userIdSequence++);
        }
        users.put(user.getId(), user);
        return user;
    }

    /**
     * 특정 ID의 사용자 조회
     */
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    /**
     * 특정 ID의 사용자가 존재하는지 확인
     */
    public boolean existsById(Long userId) {
        return users.containsKey(userId);
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
}
