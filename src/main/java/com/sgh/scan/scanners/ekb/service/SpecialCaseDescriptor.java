package com.sgh.scan.scanners.ekb.service;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("SpecialCase")
public interface SpecialCaseDescriptor extends ServiceDescriptor {

    String getLineContent();
    void setLineContent(String lineContent);
}
