package com.sgh.scan.scanners.ekb.restcontroller;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("RestEndpoint")
public interface EndpointDescriptor extends RestDescriptor {

    String getApiEndpoint();
    void setApiEndpoint(String apiEndpoint);

    String getEndpointPath();
    void setEndpointPath(String endpointPath);

    String getRequestMethod();
    void setRequestMethod(String requestMethod);

    String getBlindEndpointPath();
    void setBlindEndpointPath(String blindEndpoint);

    String getParamTypes();
    void setParamTypes(String paramTypes);
}
