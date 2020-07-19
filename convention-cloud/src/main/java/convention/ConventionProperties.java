package convention;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lanmaoly.cloud.convention")
public class ConventionProperties {

    /**
     * 外部额外的配置文件加载地址
     */
    private String externalConfigDir;

    public String getExternalConfigDir() {
        return externalConfigDir;
    }

    public void setExternalConfigDir(String externalConfigDir) {
        this.externalConfigDir = externalConfigDir;
    }

}
