package com.sgh.scan.exceptions;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

@Component
class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable ex = getError(request);
        Map<String, Object> map = new HashMap<>();
        map.put("statusCode", 500);
        map.put("status", "SERVER_ERROR");
        map.put("error", " Internal Server Error");
        map.put("message", ex.getMessage());
        return map;
    }
}
