package com.sgh.scan.common.git;

import com.sgh.scan.entities.RepositoryInfo;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class GitCloner {

    public static GitCloner getInstance() { return GitClonerHelper.INSTANCE; }
    private GitCloner() { }

    public void cloneRepository(RepositoryInfo repositoryInfo) throws Exception {
        try {
            log.info("Cloning " + repositoryInfo.getRepoUrl() + " into " + repositoryInfo.getCloneDirectoryPath());
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

    public void pushToRepository(RepositoryInfo repositoryInfo) throws Exception {
        log.info("Pushing to " + repositoryInfo.getRepoUrl());
        Path repoPath = Paths.get(repositoryInfo.getCloneDirectoryPath());
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {

            Files.writeString(repoPath.resolve("README_TEST.md"), "# test_repo");
            git.add().addFilepattern("README_TEST.md").call();
            git.commit().setMessage("first commit").setAuthor("author", "nghiahotrong89@email.com").call();

            // git remote add origin git@github.com:ralscha/test_repo.git
            var remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName("origin");
            remoteAddCommand.setUri(new URIish(repositoryInfo.getRepoUrl()));
            remoteAddCommand.call();

            // git push -u origin master
            var pushCommand = git.push();
            if (RepositoryInfo.TransportType.SSH.equals(repositoryInfo.getTransportType())) {
                pushCommand.setTransportConfigCallback(transport -> {
                    SshTransport sshTransport = (SshTransport)transport;
                    sshTransport.setSshSessionFactory(repositoryInfo.getSshInfo().getSshSessionFactory());
                });
            } else {
                pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        repositoryInfo.getUsernamePasswordInfo().getUserName(),
                        repositoryInfo.getUsernamePasswordInfo().getPassword()));
            }
            pushCommand.add(repositoryInfo.getBranch());
            pushCommand.setRemote("origin");
            pushCommand.call();
        } catch (GitAPIException e) {
            log.info("Exception occurred while pushing to " + repositoryInfo.getRepoUrl());
            throw e;
        }
        log.info("Completed Pushing to " + repositoryInfo.getRepoUrl());
    }

    public void pushToExistRepository(RepositoryInfo repositoryInfo) throws Exception {
        try {
            Path repoPath = Paths.get(repositoryInfo.getCloneDirectoryPath());

            /*log.info("Cloning " + repositoryInfo.getRepoUrl());
            var cloneCommand = Git.cloneRepository().setURI(repositoryInfo.getRepoUrl())
                    .setDirectory(repoPath.toFile());
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
            Git git = cloneCommand.call();*/

            log.info("Opening " + repositoryInfo.getRepoUrl());
            Git git = Git.open(repoPath.toFile());

            log.info("Add THIRD.MD");
            Files.writeString(repoPath.resolve("THIRD.md"), "# another file");
            git.add().addFilepattern("THIRD.md").call();
            git.commit().setMessage("third commit").setAuthor("author", "nghiahotrong@email.com").call();

            log.info("Pushing to " + repositoryInfo.getRepoUrl());
            var pushCommand = git.push();
            if (RepositoryInfo.TransportType.SSH.equals(repositoryInfo.getTransportType())) {
                pushCommand.setTransportConfigCallback(transport -> {
                    SshTransport sshTransport = (SshTransport)transport;
                    sshTransport.setSshSessionFactory(repositoryInfo.getSshInfo().getSshSessionFactory());
                });
            } else {
                pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        repositoryInfo.getUsernamePasswordInfo().getUserName(),
                        repositoryInfo.getUsernamePasswordInfo().getPassword()));
            }
            pushCommand.call();
            log.info("Completed Pushing to " + repositoryInfo.getRepoUrl());
        } catch (GitAPIException e) {
            log.info("Exception occurred while pushing to " + repositoryInfo.getRepoUrl());
            throw e;
        }
    }

    private static class GitClonerHelper { private static final GitCloner INSTANCE = new GitCloner(); }
}
