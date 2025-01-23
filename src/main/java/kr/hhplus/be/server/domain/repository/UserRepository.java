package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long userIdSequence = 1L;

    public boolean existsById(Long userId) {
        return users.containsKey(userId);
    }

    public void deleteUser(Long userId) {
        users.remove(userId);
    }


}
