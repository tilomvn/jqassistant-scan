package com.sgh.scan.scanners;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.sgh.scan.scanners.config.AbstractPluginIT;
import com.sgh.scan.scanners.config.CommonConfig;
import com.sgh.scan.scanners.config.Neo4jDbConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.util.stream.Collectors;

//TODO: Use SpringBootApplication instead SpringBootTest, but SpringBootTest can specify client's context separately
@SpringBootTest(classes = EkbScanPlugin.class)
@EnableConfigurationProperties(Neo4jDbConfiguration.class)
@ContextConfiguration(classes = { CommonConfig.class, EkbScanContext.class })
@Slf4j
public class EkbScanPlugin extends AbstractPluginIT {

    @Autowired
    EkbScanInfo ekbInfo;

    @Autowired
    EkbScanPlugin(Neo4jDbConfiguration neo4jDbConfiguration) { super(neo4jDbConfiguration); }

    //TODO: reset database and add new is good choice to reduce code complex with scan time is less than 5 mins
    @Test
    @TestStore(type = TestStore.Type.REMOTE, reset = false)
    public void scanJavaFile() throws Exception {
        var scanDir = ekbInfo.scanDir;

        if ("".equals(scanDir) || scanDir == null) {
            log.info("No scan directory found.");
        } else {
            log.info("Start scan Ekb's directory: " + scanDir);

            var scanFiles = getScanFiles(scanDir);

            if (scanFiles.size() > 0) {
                store.beginTransaction();

                //TODO: filter file extension according to client's extension requirement
                var controllers = scanFiles.stream().filter(f -> f.matches(".*Controller.java")).collect(Collectors.toList());
                for (String file : controllers) {
                    var testFile = new File(file);
                    getScanner().scan(testFile, testFile.getName(), DefaultScope.NONE);
                }

                var services = scanFiles.stream().filter(f -> f.matches(".*Service.java")).collect(Collectors.toList());
                for (String file : services) {
                    var testFile = new File(file);
                    getScanner().scan(testFile, testFile.getName(), DefaultScope.NONE);
                }

                store.commitTransaction();
            }

            log.info("End scan Ekb's directory: " + scanDir);
        }
    }
}
