package com.sgh.scan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
}
