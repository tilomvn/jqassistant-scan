package com.sgh.scan.services;

import com.sgh.scan.entities.RepositoryInfo;
import com.sgh.scan.dto.request.RepositoryInfoRequest;
import com.sgh.scan.export.DataExportService;
import com.sgh.scan.repositories.ApplicationRepository;
import com.sgh.scan.runners.JQAssistantPluginRunner;
import com.sgh.scan.scanners.EkbScanPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class ApplicationService {

    final ApplicationRepository applicationRepository;
    final JQAssistantPluginRunner jqAssistantPluginRunner;
    final DataExportService dataExportService;

    ApplicationService(DataExportService dataExportService) {
        applicationRepository = ApplicationRepository.newInstance();
        jqAssistantPluginRunner = JQAssistantPluginRunner.newInstance();
        this.dataExportService = dataExportService;
    }

    public RepositoryInfo addRepositoryInfo(RepositoryInfoRequest repositoryInfoRequest) throws Exception {
        RepositoryInfo repositoryInfo = /*new RepositoryInfo(repositoryInfoRequest.getBranch(), repositoryInfoRequest.getRepoUrl(),
                RepositoryInfo.TransportType.valueOf(repositoryInfoRequest.getAuthenticationType()))*/null;

        var repositoryInfoDescriptor =
                applicationRepository.findByRepoUrlAndBranch(repositoryInfoRequest.getRepoUrl(), repositoryInfoRequest.getBranch());
        if (repositoryInfoDescriptor == null) {
            repositoryInfoDescriptor = applicationRepository.newRepository(repositoryInfoRequest);
            if (RepositoryInfo.TransportType.SSH.name().equals(repositoryInfoRequest.getAuthenticationType())) {
                repositoryInfo.setPublicSshKey(Files.readString(Path.of(repositoryInfoDescriptor.getSshPublicKeyPath())));
            }
        } else {
            repositoryInfo.setStatus("EXISTED");
        }
        return repositoryInfo;
    }

    public void runEKBContext() {
        jqAssistantPluginRunner.runPlugin(EkbScanPlugin.class);
        dataExportService.exportData();
    }
}
