package com.shopease.auditing.aware;

import com.shopease.common.security.helper.SecurityContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@NullMarked
public class ApplicationAuditAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        UUID userId = SecurityContext.getUserIdFromSecurityContext();
        return Optional.ofNullable(userId);
    }
}
