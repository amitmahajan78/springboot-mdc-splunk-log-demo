package com.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID = "X-Correlation-ID";
    private static final String SERVICE_NAME = "sample-service";
    private static final String MDC_SERVICE_KEY = "label.service";
    private static final String MDC_ENDPOINT_KEY = "label.endpoint";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Generate or use existing correlation ID
            String correlationId = request.getHeader(CORRELATION_ID);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Set correlation ID in response header
            response.setHeader(CORRELATION_ID, correlationId);
            
            // Set MDC context
            MDC.put(CORRELATION_ID, correlationId);
            MDC.put(MDC_SERVICE_KEY, SERVICE_NAME);
            MDC.put(MDC_ENDPOINT_KEY, request.getRequestURI());
            
            filterChain.doFilter(request, response);
        } finally {
            // Clear MDC context
            MDC.remove(CORRELATION_ID);
            MDC.remove(MDC_SERVICE_KEY);
            MDC.remove(MDC_ENDPOINT_KEY);
        }
    }
} 