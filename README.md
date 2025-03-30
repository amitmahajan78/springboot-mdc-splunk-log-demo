# Spring Boot MDC Splunk Demo

This project demonstrates structured logging with MDC (Mapped Diagnostic Context) in a Spring Boot application, optimized for Splunk integration. It provides comprehensive request/response logging and error tracking with detailed context.

## Table of Contents
- [Features](#features)
- [Configuration](#configuration)
- [Logging Structure](#logging-structure)
- [Example Logs](#example-logs)
- [Splunk Integration](#splunk-integration)
- [Benefits](#benefits)

## Features

- Structured JSON logging for easy parsing
- Request/Response logging with MDC context
- Error tracking with detailed context
- Correlation ID tracking
- Session ID support
- Latency measurement
- Endpoint class tracking
- Exception type extraction
- Sanitized content logging
- Splunk-optimized format

## Configuration

### application.properties
```properties
# Server Configuration
server.port=8080

# Logging Configuration
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### logback-spring.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- MDC Fields -->
            <includeMdcKeyName>correlationId</includeMdcKeyName>
            <includeMdcKeyName>sessionId</includeMdcKeyName>
            <includeMdcKeyName>method</includeMdcKeyName>
            <includeMdcKeyName>path</includeMdcKeyName>
            <includeMdcKeyName>queryString</includeMdcKeyName>
            <includeMdcKeyName>headers</includeMdcKeyName>
            <includeMdcKeyName>requestBody</includeMdcKeyName>
            <includeMdcKeyName>status</includeMdcKeyName>
            <includeMdcKeyName>responseBody</includeMdcKeyName>
            <includeMdcKeyName>error</includeMdcKeyName>
            <includeMdcKeyName>errorType</includeMdcKeyName>
            <includeMdcKeyName>latency</includeMdcKeyName>
            <includeMdcKeyName>label</includeMdcKeyName>
            <includeMdcKeyName>endpointClass</includeMdcKeyName>
            <includeMdcKeyName>exceptionType</includeMdcKeyName>
            <includeMdcKeyName>statusCode</includeMdcKeyName>
            
            <!-- Custom Fields -->
            <customFields>{"application":"spring-boot-mdc-splunk-demo"}</customFields>
            
            <!-- Formatting -->
            <prettyPrint>true</prettyPrint>
            <timeZone>UTC</timeZone>
            <timestampPattern>yyyy-MM-dd HH:mm:ss.SSS</timestampPattern>
            
            <!-- Field Names -->
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <message>message</message>
                <logger>logger</logger>
                <thread>thread</thread>
                <level>level</level>
            </fieldNames>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>

    <logger name="com.example" level="DEBUG"/>
</configuration>
```

## Logging Structure

### Request/Response Log Fields

| Field | Description | Example |
|-------|-------------|---------|
| timestamp | UTC timestamp of the log entry | "2024-03-30 17:45:23.456" |
| level | Log level (INFO/ERROR) | "INFO" |
| thread | Thread name | "http-nio-8080-exec-5" |
| logger | Logger name | "com.example.filter.RequestResponseLoggingFilter" |
| message | Human-readable log message | "Request received - Method: GET, Path: /api/hello" |
| correlationId | Unique request identifier | "550e8400-e29b-41d4-a716-446655440000" |
| sessionId | User session identifier | "test123" |
| method | HTTP method | "GET" |
| path | Request path | "/api/hello" |
| queryString | URL query parameters | "param1=value1" |
| headers | Request headers | "accept:text/plain, user-agent:curl/7.64.1" |
| requestBody | Request body (sanitized) | "{"key":"value"}" |
| status | HTTP status code | "200" |
| responseBody | Response body (sanitized) | "{"result":"success"}" |
| latency | Request processing time in ms | "15" |
| application | Application name | "spring-boot-mdc-splunk-demo" |

### Error Log Fields

| Field | Description | Example |
|-------|-------------|---------|
| label | Set to "exception" for filtering | "exception" |
| error | Error message | "This is a sample error" |
| errorType | Exception class name | "RuntimeException" |
| statusCode | HTTP status code | "500" |
| endpointClass | Controller class name | "ApiController" |
| exceptionType | Root cause exception type | "RuntimeException" |

## Example Logs

### Request Log
```json
{
  "timestamp": "2024-03-30 17:45:23.456",
  "level": "INFO",
  "thread": "http-nio-8080-exec-5",
  "logger": "com.example.filter.RequestResponseLoggingFilter",
  "message": "Request received - Method: GET, Path: /api/hello, Query: none, SessionId: test123",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "test123",
  "method": "GET",
  "path": "/api/hello",
  "queryString": "",
  "headers": "accept:text/plain, user-agent:curl/7.64.1",
  "application": "spring-boot-mdc-splunk-demo"
}
```

### Response Log
```json
{
  "timestamp": "2024-03-30 17:45:23.471",
  "level": "INFO",
  "thread": "http-nio-8080-exec-5",
  "logger": "com.example.filter.RequestResponseLoggingFilter",
  "message": "Response sent - Status: 200, Method: GET, Path: /api/hello, Latency: 15ms, SessionId: test123",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "test123",
  "method": "GET",
  "path": "/api/hello",
  "queryString": "",
  "headers": "accept:text/plain, user-agent:curl/7.64.1",
  "status": "200",
  "responseBody": "Hello, World!",
  "latency": "15",
  "application": "spring-boot-mdc-splunk-demo"
}
```

### Error Log
```json
{
  "timestamp": "2024-03-30 17:45:23.456",
  "level": "ERROR",
  "thread": "http-nio-8080-exec-5",
  "logger": "com.example.filter.RequestResponseLoggingFilter",
  "message": "Error processing request - Method: GET, Path: /api/error, Error: This is a sample error, Latency: 15ms, SessionId: test123, EndpointClass: ApiController, ExceptionType: RuntimeException",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "test123",
  "method": "GET",
  "path": "/api/error",
  "statusCode": "500",
  "error": "This is a sample error",
  "errorType": "RuntimeException",
  "label": "exception",
  "endpointClass": "ApiController",
  "exceptionType": "RuntimeException",
  "latency": "15",
  "application": "spring-boot-mdc-splunk-demo"
}
```

### Complete Request-Response Cycle Example

Here's how the logs appear in sequence for a successful request:

1. **Request Log** (when request is received):
```json
{
  "timestamp": "2024-03-30 17:45:23.456",
  "level": "INFO",
  "thread": "http-nio-8080-exec-5",
  "logger": "com.example.filter.RequestResponseLoggingFilter",
  "message": "Request received - Method: GET, Path: /api/hello, Query: none, SessionId: test123",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "test123",
  "method": "GET",
  "path": "/api/hello",
  "queryString": "",
  "headers": "accept:text/plain, user-agent:curl/7.64.1",
  "application": "spring-boot-mdc-splunk-demo"
}
```

2. **Response Log** (after request is processed):
```json
{
  "timestamp": "2024-03-30 17:45:23.471",
  "level": "INFO",
  "thread": "http-nio-8080-exec-5",
  "logger": "com.example.filter.RequestResponseLoggingFilter",
  "message": "Response sent - Status: 200, Method: GET, Path: /api/hello, Latency: 15ms, SessionId: test123",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "test123",
  "method": "GET",
  "path": "/api/hello",
  "queryString": "",
  "headers": "accept:text/plain, user-agent:curl/7.64.1",
  "status": "200",
  "responseBody": "Hello, World!",
  "latency": "15",
  "application": "spring-boot-mdc-splunk-demo"
}
```

Key points about the request-response cycle:
1. Both logs share the same `correlationId` for request tracing
2. Both logs share the same `sessionId` for user tracking
3. The response log includes additional fields:
   - `status`: HTTP status code
   - `responseBody`: The actual response content
   - `latency`: Time taken to process the request
4. The timestamp difference shows the processing time (15ms in this example)

## Splunk Integration

### Example Splunk Searches

1. Find all errors:
```
index=main label=exception
```

2. Find errors by endpoint:
```
index=main label=exception endpointClass=ApiController
```

3. Find slow requests:
```
index=main latency>1000
```

4. Find errors by exception type:
```
index=main label=exception exceptionType=RuntimeException
```

5. Find requests by session:
```
index=main sessionId=test123
```

### Example Splunk Dashboard

```spl
| stats count by statusCode, endpointClass
| sort -count
| eval status=case(
    statusCode=="200", "Success",
    statusCode=="400", "Bad Request",
    statusCode=="500", "Server Error",
    true(), statusCode
)
| chart values(count) by status, endpointClass
```

## Benefits

1. **Structured Logging**
   - JSON format for easy parsing
   - Consistent field names and types
   - Better searchability in Splunk

2. **Request Tracking**
   - Correlation ID for request tracing
   - Session ID for user tracking
   - Complete request/response context

3. **Error Monitoring**
   - Detailed error context
   - Exception type tracking
   - Endpoint-specific error rates

4. **Performance Monitoring**
   - Latency tracking
   - Endpoint performance metrics
   - Slow request identification

5. **Security**
   - Sensitive data sanitization
   - Header filtering
   - Content length limits

6. **Debugging**
   - Complete request/response cycle
   - Exception stack traces
   - Context preservation

7. **Analytics**
   - Error rate analysis
   - Performance trends
   - Usage patterns

8. **Maintenance**
   - Easy configuration
   - Extensible structure
   - Clear documentation 