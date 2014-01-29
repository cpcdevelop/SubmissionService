/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.wmin.cpc.submission.frontend.helpers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 * @author Benoit Meilhac <B.Meilhac@westminster.ac.uk>
 */
public class PropertiesManager {
    
    private String defaultDciBridgeLocation;
    private String defaultRepositoryLocation;
    private String defaultStorageLocation;
    private String defaultCleaningExecutable;
    private String defaultServerLocation;
    private String defaultLoggingMode;
    
    public PropertiesManager() {
    }
    
    public void readProperties() throws Exception {
        Path path = getPropertiesPath();
        if (!Files.exists(path)) {
            throw new Exception("No properties file found");
        }
        
        String idNameProp = "submission.default.";
        Properties propFile = new Properties();
        propFile.load(Files.newInputStream(path));
        
        String valueServer = propFile.getProperty(idNameProp + "server");
        String valueDCI = propFile.getProperty(idNameProp + "dciBridge");
        String valueRepo = propFile.getProperty(idNameProp + "repository");
        String valueStore = propFile.getProperty(idNameProp + "storage");
        String valueClean = propFile.getProperty(idNameProp + "cleaning");
        String valueLog = propFile.getProperty(idNameProp + "log4j");
        
        defaultServerLocation = valueServer;
        defaultDciBridgeLocation = valueDCI;
        defaultRepositoryLocation = valueRepo;
        defaultStorageLocation = valueStore;
        defaultCleaningExecutable = checkInt(valueClean);
        defaultLoggingMode = (valueLog == null ? valueLog : valueLog.toUpperCase());
    }
    
    private String checkInt(String integer) {
        try {
            Integer.parseInt(integer);
            return integer;
        } catch (NumberFormatException ex) {
        }
        
        return null;
    }
    
    public void writeProperties() throws Exception {
        String idNameProp = "submission.default.";
        Path path = getPropertiesPath();
        Properties file = new Properties();
        
        writeProperty(file, idNameProp + "server", defaultServerLocation);
        writeProperty(file, idNameProp + "dciBridge", defaultDciBridgeLocation);
        writeProperty(file, idNameProp + "repository", defaultRepositoryLocation);
        writeProperty(file, idNameProp + "storage", defaultStorageLocation);
        writeProperty(file, idNameProp + "cleaning", defaultCleaningExecutable);
        writeProperty(file, idNameProp + "log4j", defaultLoggingMode);
        
        file.store(Files.newOutputStream(path), "AutoGenerated by the Submission"
                + " Service");
    }
    
    private void writeProperty(Properties propFile, String key, String value) {
        if (key != null && value != null) {
            propFile.setProperty(key, value);
        }
    }
    
    public String getDefaultDciBridgeLocation() {
        return defaultDciBridgeLocation;
    }
    
    public void setDefaultDciBridgeLocation(String defaultDciBridgeLocation) {
        this.defaultDciBridgeLocation = defaultDciBridgeLocation;
    }
    
    public String getDefaultRepositoryLocation() {
        return defaultRepositoryLocation;
    }
    
    public void setDefaultRepositoryLocation(String defaultRepositoryLocation) {
        this.defaultRepositoryLocation = defaultRepositoryLocation;
    }
    
    public String getDefaultStorageLocation() {
        return defaultStorageLocation;
    }
    
    public void setDefaultStorageLocation(String defaultStorageLocation) {
        this.defaultStorageLocation = defaultStorageLocation;
    }
    
    public String getDefaultCleaningExecutable() {
        return defaultCleaningExecutable;
    }
    
    public void setDefaultCleaningExecutable(String defaultCleaningExecutable) {
        this.defaultCleaningExecutable = defaultCleaningExecutable;
    }
    
    public String getDefaultServerLocation() {
        return defaultServerLocation;
    }
    
    public void setDefaultServerLocation(String defaultServerLocation) {
        this.defaultServerLocation = defaultServerLocation;
    }
    
    public String getDefaultLoggingMode() {
        return defaultLoggingMode;
    }
    
    public void setDefaultLoggingMode(String defaultLoggingMode) {
        this.defaultLoggingMode = defaultLoggingMode;
    }
    
    private Path getPropertiesPath() throws Exception {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new Exception("No user home defined");
        }
        
        return Paths.get(userHome, "submission-service.properties");
    }
}
