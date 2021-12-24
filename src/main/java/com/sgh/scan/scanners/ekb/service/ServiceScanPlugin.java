package com.sgh.scan.scanners.ekb.service;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.sgh.scan.scanners.ekb.restcontroller.EndpointDescriptor;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.file.Files;

import static com.sgh.scan.common.CommonConstants.*;

@Requires(FileDescriptor.class)
@Slf4j
public class ServiceScanPlugin extends AbstractScannerPlugin<FileResource, FileServiceDescriptor> {

    @Override
    public boolean accepts(FileResource fileResource, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith("service.java");
    }

    void createSpecialCase(final Store store, String content) {
        var specialCaseDescriptors = store.executeQuery("MATCH (n:SpecialCase) " +
                "WHERE n.lineContent = '" + content + "' RETURN n");
        if (!specialCaseDescriptors.hasResult()) {
            SpecialCaseDescriptor specialCaseDescriptor = store.create(SpecialCaseDescriptor.class);
            specialCaseDescriptor.setLineContent(content);
        }
    }

    EndpointCallerDescriptor isExistEndpointCaller(final Store store, String funcName, String microservice) {
        var endpointCallerDescriptors = store.executeQuery("MATCH (n:EndpointCaller) " +
                "WHERE n.value = '" + funcName + "' and n.microservice = '" + microservice + "' RETURN n");
        if (endpointCallerDescriptors.hasResult()) {
            return endpointCallerDescriptors.getSingleResult().get("n", EndpointCallerDescriptor.class);
        }
        return null;
    }

    EndpointCallerDescriptor createEndpointCallerDescriptor(final Store store, String funcName, String microservice) {
        var endpointCallerDescriptor = store.create(EndpointCallerDescriptor.class);
        endpointCallerDescriptor.setValue(funcName);
        endpointCallerDescriptor.setMicroservice(microservice);
        return endpointCallerDescriptor;
    }

    RemoteServiceDescriptor isExistRemoteServiceDescriptor(final Store store, String apiEndpoint, String endpointPath,
                                                          String requestMethod, String microservice) {
        var remoteServiceDescriptors = store.executeQuery("MATCH (n:RemoteService) " +
                "WHERE n.endpointFullPath = '" + endpointPath + "' " +
                "AND n.apiEndpoint = '" + apiEndpoint + "' " +
                "AND n.requestMethod = '" + requestMethod + "' " +
                "AND n.microservice = '" + microservice + "' RETURN n");
        if (remoteServiceDescriptors.hasResult()) {
            return remoteServiceDescriptors.getSingleResult().get("n", RemoteServiceDescriptor.class);
        }
        return null;
    }

    RemoteServiceDescriptor createRemoteServiceDescriptor(final Store store, String apiEndpoint, String endpointPath,
                                                          String requestMethod, String microservice) {
        var remoteServiceDescriptor = store.create(RemoteServiceDescriptor.class);
        remoteServiceDescriptor.setValue(apiEndpoint);
        remoteServiceDescriptor.setApiEndpoint(apiEndpoint);
        remoteServiceDescriptor.setEndpointFullPath(endpointPath);
        remoteServiceDescriptor.setRequestMethod(requestMethod);
        remoteServiceDescriptor.setMicroservice(microservice);
        return remoteServiceDescriptor;
    }

    @Override
    public FileServiceDescriptor scan(FileResource fileResource, String path, Scope scope, Scanner scanner) throws IOException {
        var context = scanner.getContext();
        final var store = context.getStore();
        final var fileDescriptor = context.getCurrentDescriptor();

        var fileContents = Files.readAllLines(fileResource.getFile().toPath());
        var fileName = fileResource.getFile().getName();
        var fileServiceDescriptorValue = fileName.replace(".java", BLANK);
        int i;
        String lineContent;

        //create service node
        FileServiceDescriptor fileServiceDescriptor = null;
        for (i = 0; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            if (lineContent.startsWith("public interface") || lineContent.startsWith("public abstract")) { return null; }
            if (lineContent.contains(" RestTemplate restTemplate;")) {
                var fileServiceDescriptors = store.executeQuery("MATCH (s:FileService) WHERE s.value = '" + fileServiceDescriptorValue + "' RETURN s");
                if (fileServiceDescriptors.hasResult()) {
                    fileServiceDescriptor = fileServiceDescriptors.getSingleResult().get("s", FileServiceDescriptor.class);
                } else {
                    fileServiceDescriptor = store.addDescriptorType(fileDescriptor, FileServiceDescriptor.class);
                    fileServiceDescriptor.setValue(fileServiceDescriptorValue);
                }
                i++;
                break;
            }
        }

        //constructor pass
        for (; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            if (lineContent.contains("public " + fileServiceDescriptorValue + "(")) { continue; }
            if (lineContent.endsWith(END_FUNC)) { break; }
        }

        //function checking
        for (; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            if (lineContent.contains("public ") || lineContent.contains("private ")) {
                //get function name
                var matcher = Pattern.compile(".* ([^']*)\\(.*").matcher(lineContent);
                matcher.matches(); String funcName = matcher.group(1);

                //create remote service
                int j = i + 1;
                while(!(lineContent = fileContents.get(j)).equals(END_FUNC)) {
                    if (lineContent.contains("restTemplate")) {
                        //get full line content
                        if (!lineContent.endsWith(END_CALL_FUNC)) {
                            var newLineContent = new StringBuilder();
                            while(true) {
                                lineContent = fileContents.get(j);
                                newLineContent.append(lineContent);
                                if (lineContent.endsWith(END_CALL_FUNC)) { break; }
                                j++;
                            }
                            lineContent = newLineContent.toString();
                        }

                        //get request method
                        matcher = Pattern.compile(".*restTemplate.([^']*)ForObject.*").matcher(lineContent);
                        if (!matcher.matches()) {
                            matcher = Pattern.compile(".*restTemplate.exchange.*HttpMethod.([^']*), .*").matcher(lineContent);
                            matcher.matches();
                        }
                        var requestMethod = matcher.group(1).toUpperCase();

                        //create endpoint caller
                        EndpointCallerDescriptor endpointCallerDescriptor;
                        String serviceName;
                        String microservice;
                        matcher = Pattern.compile(".*\\(([^',]*)\\(\".*").matcher(lineContent);
                        if (matcher.matches()) {
                            serviceName = matcher.group(1);
                            microservice = serviceName.replace("URL", BLANK);
                            //create function endpoint
                            endpointCallerDescriptor = isExistEndpointCaller(store, funcName, microservice);
                            if (endpointCallerDescriptor == null) {
                                endpointCallerDescriptor = createEndpointCallerDescriptor(store, funcName, microservice);
                                endpointCallerDescriptor.setMicroservice(microservice);
                                fileServiceDescriptor.getFuncEndpoints().add(endpointCallerDescriptor);
                            }
                        } else {
                            //TODO: log special case
                            if (lineContent.contains("WEB_HOOK")) {
                                microservice = "slack";
                                //create function endpoint
                                endpointCallerDescriptor = isExistEndpointCaller(store, funcName, microservice);
                                if (endpointCallerDescriptor == null) {
                                    endpointCallerDescriptor = createEndpointCallerDescriptor(store, funcName, microservice);
                                    fileServiceDescriptor.getFuncEndpoints().add(endpointCallerDescriptor);
                                }
                                var remoteServiceDescriptor = isExistRemoteServiceDescriptor(store,"slack", "slack", requestMethod, "slack");
                                if (remoteServiceDescriptor == null) {
                                    remoteServiceDescriptor = createRemoteServiceDescriptor(store,"slack", "slack", requestMethod, "slack");
                                    endpointCallerDescriptor.getRemoteServices().add(remoteServiceDescriptor);
                                }
                            } else {
                                //TODO: will check special case later
                                createSpecialCase(store, lineContent);
                            }

                            j++;
                            continue;
                        }
                        endpointCallerDescriptor.setRequestMethod(requestMethod);

                        //get endpoint path
                        String apiEndpoint;
                        String endpointPath;
                        matcher = Pattern.compile(".*" + serviceName + "\\(\"([^'\"]*)\", ([^'()]*)\\).*").matcher(lineContent);
                        if (matcher.matches()) {
                            apiEndpoint = matcher.group(1);
                            endpointPath = matcher.group(2);
                            //log special case
                            if (!endpointPath.matches("\"[^'\"]*\"") && !endpointPath.matches(NON_ENDPOINT)) {
                                var remoteServiceDescriptor = isExistRemoteServiceDescriptor(store, apiEndpoint, "[variable]", requestMethod, microservice);
                                if (remoteServiceDescriptor == null) {
                                    remoteServiceDescriptor = createRemoteServiceDescriptor(store, apiEndpoint, "[variable]", requestMethod, microservice);
                                    endpointCallerDescriptor.getRemoteServices().add(remoteServiceDescriptor);
                                }

                                //TODO: will check special case later
                                createSpecialCase(store, lineContent);

                                j++;
                                continue;
                            }
                        } else {
                            //TODO: will check special case later
                            createSpecialCase(store, lineContent);

                            j++;
                            continue;
                        }
                        endpointPath = endpointPath.replace(NON_ENDPOINT, BLANK).replace(QUOTES, BLANK);
                        endpointCallerDescriptor.setApiEndpoint(apiEndpoint);
                        endpointCallerDescriptor.setEndpointFullPath(endpointPath);

                        //add rest endpoint relationship
                        var endpointPathNoParam = Pattern.compile("\\{([a-zA-Z0-9]*)\\}").matcher(endpointPath.split("\\?")[0]).replaceAll("{}");
                        //TODO: handle api version
                        var endpointDescriptors = store.executeQuery("MATCH (n:RestEndpoint) " +
                                "WHERE n.blindEndpointPath = '/v1/" + endpointPathNoParam + "' " +
                                "AND n.apiEndpoint = '" + apiEndpoint + "' " +
                                "AND n.requestMethod = '" + requestMethod + "' RETURN n ORDER BY n.microservice DESC");
                        if (endpointDescriptors.hasResult()) {
                            //TODO: duplicate /v1/filter/branches
                            var e = endpointDescriptors.iterator().next().get("n", EndpointDescriptor.class);
                            var isExisted = endpointCallerDescriptor.getEndpoints().stream().anyMatch(ep -> ep.getId().equals(e.getId()));
                            if (!isExisted) {
                                endpointCallerDescriptor.getEndpoints().add(e);
                            }
                        } else {
                            //save non-exist endpoint
                            var remoteServiceDescriptor = isExistRemoteServiceDescriptor(store, apiEndpoint, endpointPath, requestMethod, microservice);
                            if (remoteServiceDescriptor == null) {
                                remoteServiceDescriptor = createRemoteServiceDescriptor(store, apiEndpoint, endpointPath, requestMethod, microservice);
                                endpointCallerDescriptor.getRemoteServices().add(remoteServiceDescriptor);
                            }
                        }
                    }
                    j++;
                }
                i = j;
            }
        }
        return fileServiceDescriptor;
    }
}
