package com.sgh.scan.entities;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.sgh.scan.scanners.config.NodeDescriptor;

@Label("RepositoryInfo")
public interface RepositoryInfoDescriptor extends NodeDescriptor {

    String getBranch();
    void setBranch(String branch);

    String getRepoUrl();
    void setRepoUrl(String repoUrl);

    String getAuthenticationType();
    void setAuthenticationType(String authenticationType);

    String getUsername();
    void setUsername(String userName);

    String getPassword();
    void setPassword(String password);

    String getCreatedDate();
    void setCreatedDate(String currentDate);

    String getPassPhase();
    void setPassPhase(String passPhase);

    String getCloneDirectory();
    void setCloneDirectory(String cloneDirectory);

    String getSshPrivateKeyPath();
    void setSshPrivateKeyPath(String sshPrivateKey);

    String getSshPublicKeyPath();
    void setSshPublicKeyPath(String sshPublicKey);
}
