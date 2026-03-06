package com.loader.facv;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
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
}
