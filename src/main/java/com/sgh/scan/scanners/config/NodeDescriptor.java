package com.sgh.scan.scanners.config;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;

public interface NodeDescriptor extends Descriptor {

    String getValue();

    void setValue(String value);
}
