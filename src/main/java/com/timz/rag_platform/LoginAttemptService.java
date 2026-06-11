package com.timz.rag_platform;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private final ConcurrentHashMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        attempts.computeIfAbsent(email, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    public boolean isBlocked(String email) {
        AtomicInteger count = attempts.get(email);
        return count != null && count.get() >= MAX_ATTEMPTS;
    }

    public int getRemainingAttempts(String email) {
        AtomicInteger count = attempts.get(email);
        if (count == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - count.get());
    }
}