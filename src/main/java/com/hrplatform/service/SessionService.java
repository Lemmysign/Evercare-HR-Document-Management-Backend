package com.hrplatform.service;

import java.util.Map;
import java.util.UUID;

public interface SessionService {
    String createSession(UUID staffId, UUID departmentId);
    Map<String, String> validateSession(String sessionToken);
    void invalidateSession(String sessionToken);
}