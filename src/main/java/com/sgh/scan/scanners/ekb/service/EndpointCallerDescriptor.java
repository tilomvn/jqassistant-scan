package com.sgh.scan.scanners.ekb.service;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.sgh.scan.scanners.ekb.restcontroller.EndpointDescriptor;

import java.util.List;

@Label("EndpointCaller")
public interface EndpointCallerDescriptor extends ServiceDescriptor {

    String getMicroservice();
    void setMicroservice(String microservice);

    String getApiEndpoint();
    void setApiEndpoint(String apiEndpoint);

    String getRequestMethod();
    void setRequestMethod(String requestMethod);

    String getEndpointFullPath();
    void setEndpointFullPath(String endpointPath);

    @Relation("CALL_TO_SERVICE")
    List<RemoteServiceDescriptor> getRemoteServices();

    @Relation("CALL_TO_ENDPOINT")
    List<EndpointDescriptor> getEndpoints();
}
