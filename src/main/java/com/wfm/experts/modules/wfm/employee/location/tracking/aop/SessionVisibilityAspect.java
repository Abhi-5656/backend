package com.wfm.experts.modules.wfm.employee.location.tracking.aop;

import com.wfm.experts.modules.wfm.employee.location.tracking.exception.SessionNotReadyException;
import com.wfm.experts.modules.wfm.employee.location.tracking.repository.TrackingSessionRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Aspect
@Component
public class SessionVisibilityAspect {

    private static final Logger log = LoggerFactory.getLogger(SessionVisibilityAspect.class);

    private final TrackingSessionRepository sessionRepo;

    @Value("${tracking.visibility.max-wait-ms:1500}")
    private long maxWaitMs;

    @Value("${tracking.visibility.poll-interval-ms:50}")
    private long pollIntervalMs;

    public SessionVisibilityAspect(TrackingSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    // Intercept calls to ingestPoint(..) and closeSession(..) on any impl of the interface
    @Around("execution(* com.wfm.experts..TrackingChunkService+.ingestPoint(..))"
            + " || execution(* com.wfm.experts..TrackingChunkService+.closeSession(..))")
    public Object ensureSessionVisible(ProceedingJoinPoint pjp) throws Throwable {
        Long sessionId = extractSessionId(pjp.getArgs());
        if (sessionId == null) {
            return pjp.proceed(); // nothing to enforce
        }

        long deadlineNs = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(maxWaitMs);
        boolean visible = sessionRepo.existsById(sessionId);

        while (!visible && System.nanoTime() < deadlineNs) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(pollIntervalMs));
            visible = sessionRepo.existsById(sessionId);
        }

        if (!visible) {
            log.warn("Session {} not visible after {} ms; requeue via exception.", sessionId, maxWaitMs);
            throw new SessionNotReadyException(sessionId);
        }

        return pjp.proceed();
    }

    private Long extractSessionId(Object[] args) {
        if (args == null) return null;
        for (Object a : args) {
            if (a == null) continue;

            if (a instanceof Long) return (Long) a;
            if (a instanceof Number) return ((Number) a).longValue();

            try {
                var m = a.getClass().getMethod("getSessionId");
                Object val = m.invoke(a);
                if (val instanceof Number) return ((Number) val).longValue();
            } catch (NoSuchMethodException ignored) {
            } catch (Exception reflectErr) {
                // best-effort; ignore
            }
        }
        return null;
    }
}
