package com.sgh.scan.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RepositoryInfoRequest {
    String branch;
    String repoUrl;
    String authenticationType;//HTTP or SSH
    String userName;
    String password;
    String passPhase;
}
