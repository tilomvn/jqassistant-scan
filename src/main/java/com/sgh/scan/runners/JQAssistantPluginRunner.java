package com.sgh.scan.runners;

import com.sgh.scan.scanners.config.AbstractPluginIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import javax.naming.OperationNotSupportedException;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Slf4j
public class JQAssistantPluginRunner {

    public static JQAssistantPluginRunner newInstance() { return new JQAssistantPluginRunner(); }
    private JQAssistantPluginRunner() { listener = new SummaryGeneratingListener(); }

    SummaryGeneratingListener listener;

    public void runPlugin(Class<? extends AbstractPluginIT> plugin) {
        var request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(plugin))
                .build();
        var launcher = LauncherFactory.create();
        var testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        listener.getSummary().printTo(new PrintWriter(System.out));
    }

    public void runAllPlugin() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Operation current not supported");
        /*LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("com.sgh.scan.scanners"))
                .filters(includeClassNamePatterns(".*Plugin"))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);*/
    }
}
