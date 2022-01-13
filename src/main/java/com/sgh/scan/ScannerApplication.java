package com.sgh.scan;

import com.sgh.scan.common.git.GitCloner;
import com.sgh.scan.entities.RepositoryInfo;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(
		basePackages = { "com.sgh.scan" },
		excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.sgh.scan.scanners.*")
)
//@EnableScheduling
public class ScannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScannerApplication.class, args);
	}

	@Bean
	ApplicationRunner runner() {
		return args -> {
			var git = GitCloner.getInstance();
			git.pushToExistRepository(RepositoryInfo.builder()
							.branch("master")
							.cloneDirectoryPath("F:\\Projects\\RadX\\testpush")
							.repoUrl("https://github.com/tilomvn/jqassistant-scan.git")
							.usernamePasswordInfo(RepositoryInfo.UsernamePasswordInfo.builder()
									.userName("tilomvn")
									.password("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")
									.build())
					.build());
		};
	}
}
