package com.sgh.scan.scanners.ekb.service;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("RemoteService")
public interface RemoteServiceDescriptor extends ServiceDescriptor {

    String getApiEndpoint();
    void setApiEndpoint(String serviceName);

    String getEndpointFullPath();
    void setEndpointFullPath(String endpointFullPath);

    String getRequestMethod();
    void setRequestMethod(String requestMethod);

    String getMicroservice();
    void setMicroservice(String microservice);
}