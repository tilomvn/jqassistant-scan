package com.sgh.scan.repositories;

import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.sgh.scan.dto.request.RepositoryInfoRequest;
import com.sgh.scan.entities.RepositoryInfo;
import com.sgh.scan.entities.RepositoryInfoDescriptor;
import com.sgh.scan.entities.SshInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class ApplicationRepository extends IRepository {

    public static ApplicationRepository newInstance() { return new ApplicationRepository(); }
    private ApplicationRepository() {}

    public RepositoryInfoDescriptor newRepository(RepositoryInfoRequest repositoryInfo) throws Exception {
        createConnection();
        beginTransaction("New Repository " + repositoryInfo.getRepoUrl());

        var repositoryInfoDescriptor = store.create(RepositoryInfoDescriptor.class);
        repositoryInfoDescriptor.setRepoUrl(repositoryInfo.getRepoUrl());
        repositoryInfoDescriptor.setBranch(repositoryInfo.getBranch());
        repositoryInfoDescriptor.setAuthenticationType(repositoryInfo.getAuthenticationType());//HTTP or SSH
        repositoryInfoDescriptor.setUsername(repositoryInfo.getUserName());
        repositoryInfoDescriptor.setPassword(repositoryInfo.getPassword());
        var matcher = Pattern.compile(".*\\/([^'\\/]*).git").matcher(repositoryInfo.getRepoUrl());
        if (matcher.matches()) {
            repositoryInfoDescriptor.setCloneDirectory(baseRepositoryDir + matcher.group(1));
        }
        var createdDate = getCurrentDate();
        repositoryInfoDescriptor.setCreatedDate(createdDate);
        if (RepositoryInfo.TransportType.SSH.name().equals(repositoryInfo.getAuthenticationType())) {
            repositoryInfoDescriptor.setPassPhase(repositoryInfo.getPassPhase());
            var sshInfo = SshInfo.newInstance();
            sshInfo.generateJSchKeys("RSA", "id_rsa_" + createdDate, repositoryInfo.getPassPhase(), "Admin@Sgh");
            repositoryInfoDescriptor.setSshPrivateKeyPath(sshInfo.getSshPrivateKey());
            repositoryInfoDescriptor.setSshPublicKeyPath(sshInfo.getSshPublicKey());
        }

        commitTransaction("New Repository " + repositoryInfo.getRepoUrl());
        return repositoryInfoDescriptor;
    }

    public RepositoryInfoDescriptor findByRepoUrl(String repoUrl) throws Exception {
        createConnection();
        RepositoryInfoDescriptor repositoryInfoDescriptor = null;
        Result<CompositeRowObject> results =
                store.executeQuery("MATCH (r:RepositoryInfo) WHERE r.repoUrl = '" + repoUrl + "' RETURN r");
        if (results.hasResult()) {
            repositoryInfoDescriptor = results.getSingleResult().get("r", RepositoryInfoDescriptor.class);
        }
        return repositoryInfoDescriptor;
    }

    public RepositoryInfoDescriptor findByRepoUrlAndBranch(String repoUrl, String branch) throws Exception {
        createConnection();
        RepositoryInfoDescriptor repositoryInfoDescriptor = null;
        Result<CompositeRowObject> results =
                store.executeQuery("MATCH (r:RepositoryInfo) WHERE r.repoUrl = '" + repoUrl + "' AND r.branch = '" + branch + "' RETURN r");
        if (results.hasResult()) {
            repositoryInfoDescriptor = results.getSingleResult().get("r", RepositoryInfoDescriptor.class);
        }
        return repositoryInfoDescriptor;
    }
}
