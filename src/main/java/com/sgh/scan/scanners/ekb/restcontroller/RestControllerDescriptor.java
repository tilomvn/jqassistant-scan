package com.sgh.scan.scanners.ekb.restcontroller;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("RestController")
public interface RestControllerDescriptor extends RestDescriptor, FileDescriptor {

    @Relation("DECLARES_ENDPOINT")
    List<EndpointDescriptor> getEndpoints();
}
