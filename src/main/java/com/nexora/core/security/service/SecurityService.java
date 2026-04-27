package com.nexora.core.security.service;

import java.util.UUID;

public interface SecurityService {
    UUID getCurrentUserId();
    String getCurrentUserEmail();
}
