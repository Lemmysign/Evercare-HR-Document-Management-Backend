package com.hrplatform.service.impl;

import com.hrplatform.exception.UnauthorizedException;
import com.hrplatform.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    // In-memory session store (OK for development/single instance)
    private final Map<String, Map<String, String>> sessionStore = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT = 3600; // 1 hour

    @Override
    public String createSession(UUID staffId, UUID departmentId) {
        String token = UUID.randomUUID().toString();

        Map<String, String> sessionData = Map.of(
                "staffId", staffId.toString(),
                "departmentId", departmentId.toString(),
                "createdAt", Instant.now().toString(),
                "expiresAt", Instant.now().plusSeconds(SESSION_TIMEOUT).toString()
        );

        sessionStore.put(token, sessionData);
        log.info("Session created for staff: {}", staffId);

        return token;
    }

    @Override
    public Map<String, String> validateSession(String sessionToken) {
        Map<String, String> sessionData = sessionStore.get(sessionToken);

        if (sessionData == null) {
            throw new UnauthorizedException("Session expired or invalid");
        }

        Instant expiresAt = Instant.parse(sessionData.get("expiresAt"));
        if (Instant.now().isAfter(expiresAt)) {
            sessionStore.remove(sessionToken);
            throw new UnauthorizedException("Session expired");
        }

        return sessionData;
    }

    @Override
    public void invalidateSession(String sessionToken) {
        sessionStore.remove(sessionToken);
        log.info("Session invalidated: {}", sessionToken);
    }

    // ✅ ADD THIS: Cleanup expired sessions every 1hr
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 ms
    public void cleanupExpiredSessions() {

        Instant now = Instant.now();
        AtomicInteger removedCount = new AtomicInteger();

        sessionStore.entrySet().removeIf(entry -> {
            String expiresAtStr = entry.getValue().get("expiresAt");
            Instant expiresAt = Instant.parse(expiresAtStr);

            if (now.isAfter(expiresAt)) {
                removedCount.getAndIncrement();
                return true;
            }
            return false;
        });

        if (removedCount.get() > 0) {
            log.info("Cleaned up {} expired sessions", removedCount);
        }
    }
}