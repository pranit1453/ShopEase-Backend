package com.shopease.common.annotation;

import java.lang.annotation.*;

/**

 1. Purpose:
 - Custom marker annotation used to trigger logging via AOP
 - Works with LoggingAspect to intercept method execution

 2. Annotations used:

 @Target(ElementType.METHOD)
 → Can be applied ONLY on methods

 @Retention(RetentionPolicy.RUNTIME)
 → Available at runtime (required for AOP to detect it)

 3. How it works:

 - When a method is annotated with @LogExecution:
 → LoggingAspect intercepts it using @Around advice
 → Logs:
 ✔ Method start
 ✔ Execution time
 ✔ Errors (if any)

 4. Flow:

 Method call → AOP intercepts → LoggingAspect runs →
 Actual method executes → Logs result/time/error

 ✔ Lightweight and reusable across services

 A simple annotation to enable automatic method-level logging using AOP
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecution {
}
