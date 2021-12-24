package com.sgh.scan.scanners.ekb.service;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("FileService")
public interface FileServiceDescriptor extends ServiceDescriptor, FileDescriptor {

    @Relation("DECLARES_ENDPOINT_CALLER")
    List<EndpointCallerDescriptor> getFuncEndpoints();
}
