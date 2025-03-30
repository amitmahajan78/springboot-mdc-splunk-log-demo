package com.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList("authorization", "cookie", "set-cookie");

    @Around("execution(* com.example.controller..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse());

        try {
            // Log request
            logRequest(requestWrapper, joinPoint);

            // Execute the method
            Object result = joinPoint.proceed();

            // Log response
            logResponse(responseWrapper, result, startTime);

            // Copy response body back to response
            responseWrapper.copyBodyToResponse();
            return result;

        } catch (Exception e) {
            // Log exception
            logException(e, startTime);
            throw e;
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ProceedingJoinPoint joinPoint) {
        try {
            String requestBody = new String(request.getContentAsByteArray());
            String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
            
            // Filter sensitive headers
            String headers = Collections.list(request.getHeaderNames()).stream()
                    .filter(header -> !SENSITIVE_HEADERS.contains(header.toLowerCase()))
                    .map(header -> header + "=" + request.getHeader(header))
                    .collect(Collectors.joining(", "));

            logger.info("[REQUEST] Method={}, URI={}{}, Headers={}, Body={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    queryString,
                    headers,
                    requestBody.isEmpty() ? "{}" : requestBody);
        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, Object result, long startTime) {
        try {
            String responseBody = new String(response.getContentAsByteArray());
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[RESPONSE] Status={}, Duration={}ms, Body={}",
                    response.getStatus(),
                    duration,
                    responseBody.isEmpty() ? objectMapper.writeValueAsString(result) : responseBody);
        } catch (Exception e) {
            logger.error("Error logging response", e);
        }
    }

    private void logException(Exception e, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[EXCEPTION] Duration={}ms, Error={}", duration, e.getMessage(), e);
        } catch (Exception ex) {
            logger.error("Error logging exception", ex);
        }
    }
} 