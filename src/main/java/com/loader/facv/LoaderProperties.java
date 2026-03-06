package com.loader.facv;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loader")
public class LoaderProperties {

    private boolean enabled = true;
    private boolean runOnStartup = false;
    private String inputDirectory = "/opt/loader/input";
    private List<String> fileNames = new ArrayList<String>();
    private String charset = "UTF-8";
    private int batchSize = 1000;
    private String scheduleCron = "0 59 23 * * *";
    private String scheduleZone = "Asia/Hong_Kong";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRunOnStartup() {
        return runOnStartup;
    }

    public void setRunOnStartup(boolean runOnStartup) {
        this.runOnStartup = runOnStartup;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public String getScheduleZone() {
        return scheduleZone;
    }

    public void setScheduleZone(String scheduleZone) {
        this.scheduleZone = scheduleZone;
    }
}
