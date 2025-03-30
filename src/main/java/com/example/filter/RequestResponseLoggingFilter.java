package com.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String SESSION_ID_HEADER = "sessionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Wrap request and response to cache their content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        String correlationId = UUID.randomUUID().toString();
        String sessionId = request.getHeader(SESSION_ID_HEADER);
        long startTime = System.currentTimeMillis();
        
        try {
            // Add request details to MDC before processing
            MDC.put("correlationId", correlationId);
            MDC.put("sessionId", sessionId != null ? sessionId : "");
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());
            MDC.put("queryString", request.getQueryString() != null ? request.getQueryString() : "");
            MDC.put("headers", getHeaders(request));
            
            // Set correlation ID and session ID in response header
            responseWrapper.setHeader("X-Correlation-ID", correlationId);
            if (sessionId != null) {
                responseWrapper.setHeader("X-Session-ID", sessionId);
            }
            
            // Log request
            logger.info("Request received - Method: {}, Path: {}, Query: {}, SessionId: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    request.getQueryString() != null ? request.getQueryString() : "none",
                    sessionId != null ? sessionId : "");
            
            // Process the request
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            // Calculate latency
            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;
            
            // Add response details to MDC
            MDC.put("status", String.valueOf(responseWrapper.getStatus()));
            MDC.put("latency", String.valueOf(latency));
            
            // Get request and response body
            String requestBody = new String(requestWrapper.getContentAsByteArray());
            String responseBody = new String(responseWrapper.getContentAsByteArray());
            
            // Log safely (you might want to limit or mask sensitive data)
            if (!requestBody.isEmpty()) {
                MDC.put("requestBody", sanitizeContent(requestBody));
            }
            if (!responseBody.isEmpty()) {
                MDC.put("responseBody", sanitizeContent(responseBody));
            }
            
            // Log response with latency
            logger.info("Response sent - Status: {}, Method: {}, Path: {}, Latency: {}ms, SessionId: {}", 
                    responseWrapper.getStatus(),
                    request.getMethod(), 
                    request.getRequestURI(),
                    latency,
                    sessionId != null ? sessionId : "");
            
            // Copy content back to response
            responseWrapper.copyBodyToResponse();
            
        } catch (Exception e) {
            // Calculate latency even for errors
            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;
            MDC.put("latency", String.valueOf(latency));
            
            // Enhanced error logging
            MDC.put("label", "exception");
            MDC.put("error", e.getMessage());
            MDC.put("errorType", e.getClass().getSimpleName());
            MDC.put("statusCode", String.valueOf(response.getStatus()));
            
            // Extract endpoint class from request
            String endpointClass = extractEndpointClass(request);
            MDC.put("endpointClass", endpointClass);
            
            // Extract exception type from stack trace
            String exceptionType = extractExceptionType(e);
            MDC.put("exceptionType", exceptionType);
            
            logger.error("Error processing request - Method: {}, Path: {}, Error: {}, Latency: {}ms, SessionId: {}, EndpointClass: {}, ExceptionType: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    e.getMessage(),
                    latency,
                    sessionId != null ? sessionId : "",
                    endpointClass,
                    exceptionType,
                    e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
    
    private String getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .filter(header -> !header.toLowerCase().contains("authorization")) // Skip sensitive headers
                .map(header -> header + ":" + request.getHeader(header))
                .collect(Collectors.joining(", "));
    }
    
    private String sanitizeContent(String content) {
        // Limit content length and remove sensitive data
        int maxLength = 1000;
        if (content.length() > maxLength) {
            content = content.substring(0, maxLength) + "...";
        }
        // You might want to add more sanitization logic here
        return content;
    }
    
    private String extractEndpointClass(HttpServletRequest request) {
        try {
            // Get the request URI and remove leading slash
            String path = request.getRequestURI().substring(1);
            // Split by slash and get the first part (e.g., "api")
            String[] parts = path.split("/");
            if (parts.length > 1) {
                // Convert to PascalCase for class name
                return parts[1].substring(0, 1).toUpperCase() + 
                       parts[1].substring(1) + "Controller";
            }
        } catch (Exception e) {
            // If any error occurs, return unknown
        }
        return "UnknownController";
    }
    
    private String extractExceptionType(Exception e) {
        try {
            // Get the root cause of the exception
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            return rootCause.getClass().getSimpleName();
        } catch (Exception ex) {
            return "UnknownException";
        }
    }
} 