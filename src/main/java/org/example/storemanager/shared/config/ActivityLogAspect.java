package org.example.storemanager.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.storemanager.modules.system.entity.ActivityLog;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.ActivityLogRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class ActivityLogAspect {

    private final EntityManager entityManager;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ActivityLogAspect(EntityManager entityManager,
                             ActivityLogRepository activityLogRepository,
                             UserRepository userRepository,
                             ObjectMapper objectMapper) {
        this.entityManager = entityManager;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(logActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        String actionType = logActivity.actionType();
        String entityName = logActivity.entityName();
        Class<?> entityClass = logActivity.entityClass();

        Long entityId = null;
        String oldValueJson = null;

        // Fetch entity from DB before execution for non-CREATE actions
        if (!"CREATE".equalsIgnoreCase(actionType)) {
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Long) {
                    entityId = (Long) arg;
                    break;
                }
            }
            if (entityId != null) {
                try {
                    Object oldEntity = entityManager.find(entityClass, entityId);
                    if (oldEntity != null) {
                        oldValueJson = objectMapper.writeValueAsString(oldEntity);
                    }
                } catch (Exception e) {
                    // Silently ignore or log serialization exceptions
                }
            }
        }

        // Proceed with the actual execution of the service method
        Object result = joinPoint.proceed();

        // Capture new state after execution
        String newValueJson = null;
        if ("CREATE".equalsIgnoreCase(actionType)) {
            if (result != null) {
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object idVal = getIdMethod.invoke(result);
                    if (idVal instanceof Long) {
                        entityId = (Long) idVal;
                    }
                } catch (Exception e) {
                    // Reflection fallback
                }
            }
            if (entityId != null) {
                try {
                    Object newEntity = entityManager.find(entityClass, entityId);
                    if (newEntity != null) {
                        newValueJson = objectMapper.writeValueAsString(newEntity);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        } else if ("UPDATE".equalsIgnoreCase(actionType) || "UPDATE_STATUS".equalsIgnoreCase(actionType)) {
            if (entityId != null) {
                try {
                    Object newEntity = entityManager.find(entityClass, entityId);
                    if (newEntity != null) {
                        newValueJson = objectMapper.writeValueAsString(newEntity);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        // Resolve currently logged-in user
        User user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }

        // Resolve client IP Address
        String ipAddress = "unknown";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
        }

        // Construct and save the ActivityLog
        ActivityLog log = ActivityLog.builder()
                .actionType(actionType)
                .entityName(entityName)
                .entityId(entityId)
                .oldValue(oldValueJson)
                .newValue(newValueJson)
                .ipAddress(ipAddress)
                .user(user)
                .build();

        log.setCreatedBy(user != null ? user.getUsername() : "SYSTEM");

        activityLogRepository.save(log);

        return result;
    }
}
