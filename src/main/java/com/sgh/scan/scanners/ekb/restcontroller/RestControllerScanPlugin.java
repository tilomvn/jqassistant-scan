package com.sgh.scan.scanners.ekb.restcontroller;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static com.sgh.scan.common.CommonConstants.*;

@Requires(FileDescriptor.class)
@Slf4j
public class RestControllerScanPlugin extends AbstractScannerPlugin<FileResource, RestControllerDescriptor> {

    @Override
    public boolean accepts(FileResource fileResource, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith("controller.java");
    }

    @Override
    public RestControllerDescriptor scan(FileResource fileResource, String path, Scope scope, Scanner scanner) throws IOException {
        var context = scanner.getContext();
        final var store = context.getStore();

        final var fileDescriptor = context.getCurrentDescriptor();
        RestControllerDescriptor restControllerDescriptor = null;

        var fileContents = Files.readAllLines(fileResource.getFile().toPath());
        var fileName = fileResource.getFile().getName();
        var fileDescriptorValue = fileName.replace(".java", BLANK);

        var Annotation_Request_Mapping = "@RequestMapping";
        int i;
        String lineContent;
        String apiEndpoint = null;

        //create File Description
        for (i = 0; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            if (lineContent.startsWith("@RestController")) {
                var restControllerDescriptors = store.executeQuery("MATCH (n:RestController) WHERE n.value = '" + fileDescriptorValue + "' RETURN n");
                if (restControllerDescriptors.hasResult()) {
                    restControllerDescriptor = restControllerDescriptors.getSingleResult().get("n", RestControllerDescriptor.class);
                } else {
                    restControllerDescriptor = store.addDescriptorType(fileDescriptor, RestControllerDescriptor.class);
                    restControllerDescriptor.setValue(fileDescriptorValue);
                }
                break;
            }
        }

        //api Endpoint scan
        for (; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            var matcher = Pattern.compile(".*API_ENDPOINT = \"/api/([^']*)\";.*").matcher(lineContent);
            if (matcher.matches()) { apiEndpoint = matcher.group(1); break; }
            matcher = Pattern.compile(".*API_ENDPOINT = \"api/([^']*)\";.*").matcher(lineContent);
            if (matcher.matches()) { apiEndpoint = matcher.group(1); break; }
        }

        //constructor pass
        for (; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            if (lineContent.contains("public " + fileDescriptorValue + "(")) { continue; }
            if (lineContent.equals(END_FUNC)) { break; }
        }

        //endpoint checking
        for (; i < fileContents.size(); i++) {
            lineContent = fileContents.get(i);
            if (lineContent.contains(Annotation_Request_Mapping)) {
                //get endpoint path
                if (!lineContent.endsWith(END_DECLARE_ANNOTATION)) {
                    var fullLineContent = new StringBuilder();
                    int j = i;
                    while (true) {
                        lineContent = fileContents.get(j);
                        fullLineContent.append(lineContent);
                        if (lineContent.endsWith(END_DECLARE_ANNOTATION)) { break; }
                        j++;
                    }
                    i = j;
                    lineContent = fullLineContent.toString();
                }
                var matcher = Pattern.compile(".*\\{ ([^']*) \\}.*").matcher(lineContent);
                matcher.matches();
                var endpointPath = matcher.group(1).replace(QUOTES, BLANK).replace(SPACE, BLANK);

                //get request method
                matcher = Pattern.compile(".*RequestMethod.([^'),]*).*").matcher(lineContent);
                matcher.matches();
                var requestMethod = matcher.group(1);

                int j = i + 1;
                while(!(lineContent = fileContents.get(j)).equals(END_FUNC)) {
                    if (lineContent.contains("public void ") || lineContent.contains("public ResponseEntity")) {
                        if (!lineContent.endsWith(END_DECLARE_FUNC)) {
                            var fullLineContent = new StringBuilder();
                            var p = j;
                            while (true) {
                                lineContent = fileContents.get(p);
                                fullLineContent.append(lineContent);
                                if (lineContent.endsWith(END_DECLARE_FUNC)) { break; }
                                p++;
                            }
                            lineContent = fullLineContent.toString();
                            j = p;
                        }

                        //get function name
                        matcher = Pattern.compile(".* ([^'^@]*)\\(.*").matcher(lineContent);
                        matcher.matches();
                        var funcName = matcher.group(1);

                        //create endpoint descriptor
                        var endpointPaths = endpointPath.split(COMMA);
                        for (String ep : endpointPaths) {
                            //TODO: two services has same endpoint info ?
                            var endpointDescriptors = store.executeQuery("MATCH (e:RestEndpoint) WHERE " +
                                    "e.value = '" + funcName + "' " +
                                    "AND e.apiEndpoint = '" + apiEndpoint + "' " +
                                    "AND e.endpointPath = '" + ep + "' " +
                                    "AND e.requestMethod = '" + requestMethod + "' RETURN e");
                            if (endpointDescriptors.hasResult()) {
                                //TODO: current repository has unique endpoint
                                var endpointDescriptor = endpointDescriptors.iterator().next().get("e", EndpointDescriptor.class);
                                var isExisted = restControllerDescriptor.getEndpoints().stream().anyMatch(e -> e.getId().equals(endpointDescriptor.getId()));
                                if (!isExisted) {
                                    restControllerDescriptor.getEndpoints().add(endpointDescriptor);
                                }
                            } else {
                                var endpointDescriptor = store.create(EndpointDescriptor.class);
                                endpointDescriptor.setValue(funcName);
                                endpointDescriptor.setApiEndpoint(apiEndpoint);
                                endpointDescriptor.setEndpointPath(ep);
                                endpointDescriptor.setRequestMethod(requestMethod);
                                endpointDescriptor.setBlindEndpointPath(Pattern.compile("\\{([a-zA-Z0-9]*)\\}").matcher(ep).replaceAll("{}"));

                                //scan param and get param type
                                var paramTypes = new StringBuilder();
                                matcher = Pattern.compile(" final ([^'@,)]*)").matcher(lineContent);
                                while(matcher.find()) { paramTypes.append(matcher.group(1) + COMMA); }
                                endpointDescriptor.setParamTypes(paramTypes.length() > 0 ? paramTypes.substring(0, paramTypes.length() - 1) : BLANK);

                                restControllerDescriptor.getEndpoints().add(endpointDescriptor);
                            }
                        }
                    }
                    j++;
                }
                i = j;
            }
        }

        return restControllerDescriptor;
    }
}
