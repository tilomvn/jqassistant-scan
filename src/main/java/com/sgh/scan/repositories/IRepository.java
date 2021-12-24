package com.sgh.scan.repositories;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;
import com.sgh.scan.exceptions.ConnectionException;
import com.sgh.scan.scanners.config.AbstractPluginIT;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
abstract class IRepository {

    static PluginRepositoryImpl pluginRepository;
    Store store;
    boolean isCreatedConnection = false;
    File outputDirectory;

    String baseRepositoryDir = System.getProperty("user.dir") + "/project-base/repositories/";

    String remoteServerUrl = "bolt://localhost:7687";
    String remoteUsername = "neo4j";
    String remotePassword = "jqassistant";

    public void remoteServerUrl(String remoteServerUrl) { this.remoteServerUrl = remoteServerUrl; }
    public void remoteUsername(String remoteUsername) { this.remoteUsername = remoteUsername; }
    public void remotePassword(String remotePassword) { this.remotePassword = remotePassword; }

    void init() throws Exception {
        outputDirectory = new File("project-base/jqassistant");
        outputDirectory.mkdirs();

        ClassLoader pluginClassLoader = AbstractPluginIT.class.getClassLoader();
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();

        startStore(false, ApplicationRepository.Type.REMOTE);
        isCreatedConnection = true;
    }

    void createConnection() throws Exception {
        if (!isCreatedConnection) { init(); }
        log.info("Created Connection To " + remoteServerUrl);
    }
    void beginTransaction(String transactionDescription) {
        if (!isCreatedConnection) { throw new ConnectionException("No Connection"); }
        if (!store.hasActiveTransaction()) { store.beginTransaction(); }
        log.info("Transaction " + transactionDescription + " Started");
    }
    void commitTransaction(String transactionDescription) {
        if (store.hasActiveTransaction()) { store.commitTransaction(); }
        log.info("Transaction " + transactionDescription + " Committed");
    }

    void startStore(boolean reset, Type type) throws URISyntaxException {
        StoreConfiguration.StoreConfigurationBuilder storeConfigurationBuilder = StoreConfiguration.builder();
        switch(type) {
            case FILE:
                String fileName = "project-base/jqassistant/repository-store";
                storeConfigurationBuilder.uri((new File(fileName)).toURI());
                storeConfigurationBuilder.embedded(getEmbeddedNeo4jConfiguration());
                break;
            case MEMORY:
                storeConfigurationBuilder.uri(new URI("memory:///"));
                storeConfigurationBuilder.embedded(getEmbeddedNeo4jConfiguration());
                break;
            case REMOTE:
                storeConfigurationBuilder.uri(new URI(remoteServerUrl));
                storeConfigurationBuilder.encryption("NONE");
                storeConfigurationBuilder.username(remoteUsername).password(remotePassword);
                Properties properties = new Properties();
                properties.put("neo4j.remote.statement.log.level", "info");
                storeConfigurationBuilder.properties(properties);
                break;
            default:
                throw new AssertionError("Store type not supported: " + type);
        }

        StoreConfiguration configuration = storeConfigurationBuilder.build();
        store = StoreFactory.getStore(configuration, pluginRepository.getStorePluginRepository());
        store.start();
        if (reset) {
            store.reset();
        }
    }

    EmbeddedNeo4jConfiguration getEmbeddedNeo4jConfiguration() {
        return EmbeddedNeo4jConfiguration.builder().build();
    }

    enum Type { FILE, MEMORY, REMOTE }

    String getCurrentDate() { return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()); }
}
