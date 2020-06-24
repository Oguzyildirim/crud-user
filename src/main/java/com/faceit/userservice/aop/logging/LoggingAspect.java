package com.faceit.userservice.aop.logging;

import com.faceit.userservice.config.ProfileConstants;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.Arrays;

/**
 * logging execution of service and repository components.
 */
@Aspect
public class LoggingAspect {

  private final Environment env;

  public LoggingAspect(Environment env) {
    this.env = env;
  }

  /**
   * Pointcut that matches repositories, services, REST endpoints.
   */
  @Pointcut("within(@org.springframework.stereotype.Repository *)"
      + " || within(@org.springframework.stereotype.Service *)"
      + " || within(@org.springframework.web.bind.annotation.RestController *)")
  public void springBeanPointcut() {

  }

  /**
   * Pointcut that matches all Spring beans in the application's main packages.
   */
  @Pointcut("within(com.faceit.userservice.repository..*)" + " || within(com.faceit.userservice.service..*)"
      + " || within(com.faceit.userservice.web.rest..*)")
  public void applicationPackagePointcut() {

  }

  /**
   * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
   *
   * @param joinPoint join point we want the logger for.
   * @return {@link Logger} associated to the given {@link JoinPoint}.
   */
  private Logger logger(JoinPoint joinPoint) {
    return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
  }

  /**
   * Advice that logs methods throwing exceptions.
   *
   * @param joinPoint join point for advice.
   * @param e         exception.
   */
  @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
    if (env.acceptsProfiles(Profiles.of(ProfileConstants.SPRING_PROFILE_DEVELOPMENT))) {
      logger(joinPoint).error("Exception in {}() with cause = \'{}\' and exception = \'{}\'",
          joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);
    } else {
      logger(joinPoint).error("Exception in {}() with cause = {}", joinPoint.getSignature().getName(),
          e.getCause() != null ? e.getCause() : "NULL");
    }
  }

  /**
   * Advice that logs when a method is entered and exited.
   *
   * @param joinPoint join point for advice.
   * @return result.
   * @throws Throwable throws {@link IllegalArgumentException}.
   */
  @Around("applicationPackagePointcut() && springBeanPointcut()")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    Logger log = logger(joinPoint);
    if (log.isDebugEnabled()) {
      log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(),
          Arrays.toString(joinPoint.getArgs()));
    }
    try {
      Object result = joinPoint.proceed();
      if (log.isDebugEnabled()) {
        log.debug("Exit: {}() with result = {}", joinPoint.getSignature().getName(), result);
      }
      return result;
    } catch (IllegalArgumentException e) {
      log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()),
          joinPoint.getSignature().getName());
      throw e;
    }
  }
}
