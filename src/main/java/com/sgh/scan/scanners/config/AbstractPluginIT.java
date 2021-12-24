package com.sgh.scan.scanners.config;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerConfiguration;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPluginIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginIT.class);
    private static PluginRepositoryImpl pluginRepository;
    private File outputDirectory;
    protected Store store;
    protected Analyzer analyzer;
    protected ReportContext reportContext;
    protected InMemoryReportPlugin reportPlugin;
    protected RuleSet ruleSet;

    protected final Neo4jDbConfiguration neo4jDbConfiguration;

    public AbstractPluginIT(Neo4jDbConfiguration neo4jDbConfiguration) { this.neo4jDbConfiguration = neo4jDbConfiguration; }

    public List<String> getScanFiles(String path) throws Exception {
        List<String> scanFiles = new ArrayList<>();
        var start = Paths.get(path);
        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
            var collect = stream
                    .map(String::valueOf)
                    .filter(f -> f.matches(".*.java"))
                    .collect(Collectors.toList());
            scanFiles.addAll(collect);
        }
        return scanFiles;
    }

    @BeforeAll
    public static void initPluginRepository() {
        var pluginClassLoader = AbstractPluginIT.class.getClassLoader();
        var pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();
    }

    @AfterAll
    public static void destroyPluginRepository() { if (pluginRepository != null) { pluginRepository.destroy(); } }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        //initializeRuleSet();
        outputDirectory = new File("project-base/jqassistant");
        outputDirectory.mkdirs();
        startStore(testInfo);
        //initializeAnalyzer();
    }

    @AfterEach
    public void stopStore() { if (store != null) { store.stop(); } }

    private void initializeRuleSet() throws RuleException, IOException {
        var selectedDirectory = new File(getClassesDirectory(getClass()), "rules");
        var sources = new LinkedList();
        if (selectedDirectory.exists()) { sources.addAll(FileRuleSource.getRuleSources(selectedDirectory)); }
        sources.addAll(pluginRepository.getRulePluginRepository().getRuleSources());
        var ruleParserPlugins = pluginRepository.getRulePluginRepository().getRuleParserPlugins(RuleConfiguration.DEFAULT);
        var ruleParser = new RuleParser(ruleParserPlugins);
        ruleSet = ruleParser.parse(sources);
    }

    private void initializeAnalyzer() {
        reportContext = new ReportContextImpl(store, outputDirectory);
        reportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(getReportPlugins(getReportProperties())));
        var configuration = getAnalyzerConfiguration();
        analyzer = new AnalyzerImpl(configuration, store, getRuleInterpreterPlugins(), reportPlugin, LOGGER);
    }

    protected Map<String, Object> getScannerProperties() {
        return Collections.emptyMap();
    }

    protected AnalyzerConfiguration getAnalyzerConfiguration() {
        return new AnalyzerConfiguration();
    }

    protected Map<String, Object> getReportProperties() {
        return Collections.emptyMap();
    }

    protected Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins() {
        return pluginRepository.getAnalyzerPluginRepository().getRuleInterpreterPlugins(getRuleInterpreterProperties());
    }

    protected Map<String, Object> getRuleInterpreterProperties() {
        return Collections.emptyMap();
    }

    private void startStore(TestInfo testInfo) throws URISyntaxException {
        var method = testInfo.getTestMethod().orElseThrow(() -> new AssertionError("Unabled to get the test method for test '" + testInfo.getDisplayName() + "'."));
        var testStore = method.getAnnotation(TestStore.class);
        var type = testStore != null ? testStore.type() : TestStore.Type.MEMORY;
        var storeConfigurationBuilder = StoreConfiguration.builder();
        switch(type) {
            case FILE:
                var fileName = "project-base/jqassistant/plugin-store";
                storeConfigurationBuilder.uri((new File(fileName)).toURI());
                storeConfigurationBuilder.embedded(getEmbeddedNeo4jConfiguration());
                break;
            case MEMORY:
                storeConfigurationBuilder.uri(new URI("memory:///"));
                storeConfigurationBuilder.embedded(getEmbeddedNeo4jConfiguration());
                break;
            case REMOTE:
                storeConfigurationBuilder.uri(new URI(neo4jDbConfiguration.getHost()));
                storeConfigurationBuilder.encryption("NONE");
                storeConfigurationBuilder.username(neo4jDbConfiguration.getUser()).password(neo4jDbConfiguration.getPass());
                var properties = new Properties();
                properties.put("neo4j.remote.statement.log.level", "info");
                storeConfigurationBuilder.properties(properties);
                break;
            default:
                throw new AssertionError("Store type not supported: " + type);
        }

        var configuration = storeConfigurationBuilder.build();
        store = StoreFactory.getStore(configuration, pluginRepository.getStorePluginRepository());
        store.start();
        if (testStore != null && testStore.reset()) { store.reset(); }
    }

    protected EmbeddedNeo4jConfiguration getEmbeddedNeo4jConfiguration() {
        return EmbeddedNeo4jConfiguration.builder().build();
    }

    protected Scanner getScanner() {
        return getScanner(getScannerProperties());
    }

    protected Scanner getScanner(Map<String, Object> properties) {
        var scannerContext = new ScannerContextImpl(store, outputDirectory);
        var scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        return new ScannerImpl(getScannerConfiguration(), properties, scannerContext, scannerPluginRepository);
    }

    protected ScannerConfiguration getScannerConfiguration() {
        return new ScannerConfiguration();
    }

    protected File getClassesDirectory(Class<?> rootClass) throws IOException {
        var directory = ClasspathResource.getFile(rootClass, "/");
        Assertions.assertThat(directory.isDirectory()).describedAs("Expected %s to be a directory", new Object[]{directory.toString()}).isTrue();
        return directory;
    }

    protected Map<String, ReportPlugin> getReportPlugins(Map<String, Object> properties) {
        return getReportPlugins(reportContext, properties);
    }

    protected Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext, Map<String, Object> properties) {
        return pluginRepository.getAnalyzerPluginRepository().getReportPlugins(reportContext, properties);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    protected @interface TestStore {
        boolean reset() default true;

        Type type() default TestStore.Type.FILE;

        enum Type { FILE, MEMORY, REMOTE }
    }
}

