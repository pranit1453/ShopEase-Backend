package com.shopease.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**

 1. Purpose:
 - Provides cross-cutting logging using AOP
 - Logs method execution start, end, time taken, and errors

 2. Annotations used:
 - @Aspect → Marks this class as an Aspect (AOP logic)
 - @Component → Registers it as a Spring bean
 - @Slf4j → Provides logger (log.info, log.error)

 3. Interception logic:
 - @Around("@annotation(com.shopease.common.annotation.LogExecution)")
 - Intercepts ONLY methods annotated with @LogExecution

 4. Flow:
 a. Before method execution:
 - Logs method start
 - Captures start time

 b. During execution:
 - Calls actual method using joinPoint.proceed()

 c. After success:
 - Calculates execution time
 - Logs method completion + time taken

 d. On exception:
 - Catches exception
 - Logs error + execution time
 - Re-throws exception (does NOT swallow it)

 5. Key benefits:
 ✔ No logging code inside business logic
 ✔ Centralized logging
 ✔ Measures performance (execution time)
 ✔ Automatic error logging

 6. Usage:
 - Add @LogExecution on any method to enable logging


 ✔ Works only if AOP is enabled (@EnableAspectJAutoProxy)
 ✔ Does NOT affect methods without @LogExecution
 ✔ Logging is non-intrusive (does not change business logic)

 Clean, reusable, and automatic method-level logging across the application
 */
@Slf4j
@Component
@Aspect
public class LoggingAspect {

    @Around("@annotation(com.shopease.common.annotation.LogExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Starting method: {}", methodName);
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("Ending method: {} | Time: {} ms", methodName, timeTaken);
            return result;
        } catch (Exception exception) {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.error("Error in method: {} | Time: {} ms | Message: {}",
                    methodName,
                    timeTaken,
                    exception.getMessage(),
                    exception);
            throw exception;
        }
    }
}
