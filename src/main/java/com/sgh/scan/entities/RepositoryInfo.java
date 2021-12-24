package com.sgh.scan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class RepositoryInfo {

    String branch;
    String repoUrl;
    @Setter
    String publicSshKey;
    TransportType transportType;
    @Setter
    String status = "CREATED";

    @JsonIgnore
    String cloneDirectoryPath;
    @JsonIgnore
    UsernamePasswordInfo usernamePasswordInfo;
    @JsonIgnore
    SshInfo sshInfo;
    @JsonIgnore
    String rootDir = System.getProperty("user.dir") + "/project-base/repositories/";

    public RepositoryInfo(String branch, String repoUrl, TransportType transportType) {
        this.branch = branch;
        this.repoUrl = repoUrl;
        this.transportType = transportType;
    }

    public RepositoryInfo(String branch, String repoUrl, UsernamePasswordInfo usernamePasswordInfo) {
        this.branch = branch;
        this.repoUrl = repoUrl;
        this.cloneDirectoryPath = getCloneDirectoryPath(this.repoUrl);
        this.usernamePasswordInfo = usernamePasswordInfo;
        this.transportType = TransportType.HTTP;
    }

    public RepositoryInfo(String branch, String repoUrl, SshInfo sshInfo) {
        this.branch = branch;
        this.repoUrl = repoUrl;
        this.cloneDirectoryPath = getCloneDirectoryPath(this.repoUrl);
        this.sshInfo = sshInfo;
        this.transportType = TransportType.SSH;
    }

    private String getCloneDirectoryPath(String repoUrl) {
        Matcher matcher = Pattern.compile(".*\\/([^'\\/]*).git").matcher(repoUrl);
        if (matcher.matches()) {
            Path path = Paths.get(rootDir + matcher.group(1));
            if (path.toFile().exists()) {
                Path renamePath = Paths.get(rootDir + matcher.group(1) + "-" + getCurrentDate());
                path.toFile().renameTo(renamePath.toFile());
            }
            return rootDir + matcher.group(1);
        }
        return null;
    }

    String getCurrentDate() { return DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now()); }

    public enum TransportType { HTTP, SSH }

    @Getter
    @AllArgsConstructor
    public static class UsernamePasswordInfo {
        String userName;
        String password;
    }
}