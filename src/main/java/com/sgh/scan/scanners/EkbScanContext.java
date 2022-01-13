package com.sgh.scan.scanners;

import com.sgh.scan.common.git.GitCloner;
import com.sgh.scan.entities.RepositoryInfo;
import com.sgh.scan.entities.SshInfo;
import com.sgh.scan.scanners.config.Neo4jDbConfiguration;
import com.sgh.scan.repositories.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@Slf4j
class EkbScanContext {

    //TODO: Pattern scan
    //TODO: Service -> RestController          : almost done excepts some special cases
    //TODO: RestController -> RestController   :

    @Bean
    EkbScanInfo ekbScanInfo(Neo4jDbConfiguration neo4jDbConfiguration) throws Exception {
        log.info("Start Preparing EKB Scan Context");
        log.info("Get Repository Information");
        var applicationRepository = ApplicationRepository.newInstance();
        applicationRepository.remoteServerUrl(neo4jDbConfiguration.getHost());
        applicationRepository.remoteUsername(neo4jDbConfiguration.getUser());
        applicationRepository.remotePassword(neo4jDbConfiguration.getPass());
        var repositoryInfoDescriptor =
                applicationRepository.findByRepoUrlAndBranch("git@bitbucket.org:ekbdevops/ekb-parent.git", "develop");

        log.info("Clone Repository");
        var gitCloner = GitCloner.getInstance();
        //gitCloner.cloneRepository(/*new RepositoryInfo(repositoryInfoDescriptor.getBranch(), repositoryInfoDescriptor.getRepoUrl(),
        //        new SshInfo(repositoryInfoDescriptor.getSshPrivateKeyPath(), repositoryInfoDescriptor.getPassPhase()))*/null);

        log.info("End Preparing EKB Scan Context");
        return new EkbScanInfo(repositoryInfoDescriptor.getCloneDirectory());
    }
}
