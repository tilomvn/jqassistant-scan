package com.sgh.scan.common.git;

import com.sgh.scan.entities.RepositoryInfo;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;

import java.nio.file.Paths;

@Slf4j
public class GitCloner {

    public static GitCloner getInstance() { return GitClonerHelper.INSTANCE; }
    private GitCloner() { }

    public void cloneRepository(RepositoryInfo repositoryInfo) throws Exception {
        try {
            log.info("Cloning "+ repositoryInfo.getRepoUrl() + " into " + repositoryInfo.getCloneDirectoryPath());
            var cloneCommand = Git.cloneRepository();
            if (RepositoryInfo.TransportType.SSH.equals(repositoryInfo.getTransportType())) {
                cloneCommand.setTransportConfigCallback(transport -> {
                    SshTransport sshTransport = (SshTransport)transport;
                    sshTransport.setSshSessionFactory(repositoryInfo.getSshInfo().getSshSessionFactory());
                });
            } else {
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        repositoryInfo.getUsernamePasswordInfo().getUserName(),
                        repositoryInfo.getUsernamePasswordInfo().getPassword()));
            }
            cloneCommand.setBranch(repositoryInfo.getBranch())
                    .setURI(repositoryInfo.getRepoUrl())
                    .setDirectory(Paths.get(repositoryInfo.getCloneDirectoryPath()).toFile())
                    .call();
            log.info("Completed Cloning " + repositoryInfo.getRepoUrl());
        } catch (GitAPIException e) {
            log.info("Exception occurred while cloning " + repositoryInfo.getRepoUrl());
            throw e;
        }
    }

    private static class GitClonerHelper { private static final GitCloner INSTANCE = new GitCloner(); }
}
