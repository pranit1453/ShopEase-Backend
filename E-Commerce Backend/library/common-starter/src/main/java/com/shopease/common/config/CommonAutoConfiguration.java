package com.shopease.common.config;

import com.shopease.common.aspect.LoggingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**

 1. This module contains:
 - LoggingAspect (@Aspect)
 - CommonAutoConfiguration (@AutoConfiguration)

 2. Spring Boot looks for:
 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

 3. This file contains:
 com.shopease.config.CommonAutoConfiguration

 4. When any service adds this module as a dependency:
 -> Spring Boot automatically loads CommonAutoConfiguration

 5. CommonAutoConfiguration does:
 - Enables AOP (@EnableAspectJAutoProxy)
 - Registers LoggingAspect as a bean (@Bean)

 6. LoggingAspect:
 - Intercepts methods annotated with @LogExecution
 - Logs:
 - method start
 - execution time
 - errors (if any)


 Plug-and-play logging system across all microservices
 */
@AutoConfiguration
@EnableAspectJAutoProxy
public class CommonAutoConfiguration {

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
}
