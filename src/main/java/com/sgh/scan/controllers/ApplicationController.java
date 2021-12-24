package com.sgh.scan.controllers;

import com.sgh.scan.entities.RepositoryInfo;
import com.sgh.scan.services.ApplicationService;
import com.sgh.scan.dto.request.RepositoryInfoRequest;
import com.sgh.scan.ui.pages.HomePage;
import com.sgh.scan.ui.pages.RepositoryRegistrationPage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@AllArgsConstructor(access = AccessLevel.MODULE)
class ApplicationController {

    final ApplicationService applicationService;

    @GetMapping("/page")
    Mono<String> homePage() {
        return Mono.just(new HomePage().getPage("Home"));
    }

    @GetMapping("/page/repository")
    Mono<String> repositoryRegistrationPage() {
        return Mono.just(new RepositoryRegistrationPage().getPage("Repository Registration"));
    }

    @GetMapping("/scan/ekb")
    Mono<?> runEkbScan() {
        applicationService.runEKBContext();
        return Mono.just("{ status : \"Done\" }");
    }

    @PostMapping(value = "/repository/add")
    Mono<RepositoryInfo> addRepositoryInfo(@RequestBody RepositoryInfoRequest repositoryInfo) throws Exception {
        return Mono.just(applicationService.addRepositoryInfo(repositoryInfo));
    }
}
