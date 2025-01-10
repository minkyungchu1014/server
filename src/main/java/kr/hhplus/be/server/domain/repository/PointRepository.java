package kr.hhplus.be.server.domain.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PointRepository {

    private final Map<Long, Long> userPoints = new HashMap<>();
    private final Map<Long, List<PointHistory>> pointHistories = new HashMap<>();

    public Long getPoint(Long userId) {
        return userPoints.getOrDefault(userId, 0L);
    }

    public void updatePoint(Long userId, Long newPoint) {
        userPoints.put(userId, newPoint);
    }

    public void addPointHistory(Long userId, Long amount, String type, String description) {
        PointHistory history = new PointHistory(amount, type, description);
        pointHistories.computeIfAbsent(userId, k -> new ArrayList<>()).add(history);
    }


    private static class PointHistory {
        private final Long amount;
        private final String type;
        private final String description;

        public PointHistory(Long amount, String type, String description) {
            this.amount = amount;
            this.type = type;
            this.description = description;
        }

        @Override
        public String toString() {
            return "Type: " + type + ", Amount: " + amount + ", Description: " + description;
        }
    }
}
