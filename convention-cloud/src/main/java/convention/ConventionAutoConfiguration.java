package convention;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@EnableConfigurationProperties(ConventionProperties.class)
public class ConventionAutoConfiguration {

    /**
     * 提供额外的配置文件加载地址
     */
    @Configuration
    @PropertySource(value = {
            "classpath:convention-cloud-convention.properties",
            "file:/work/config/${spring.application.name}/application.properties",
            "file:/work/config/${spring.application.name}/application.yml",
            "${convention.cloud.convention.external-config-dir}/application.properties",
            "${convention.cloud.convention.external-config-dir}/application.yml"},
            ignoreResourceNotFound = true)
    public static class ExtraPropertyAutoConfiguration {

    }
}
