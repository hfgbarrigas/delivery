package io.hfgbarrigas.delivery.configuration;

import io.hfgbarrigas.delivery.properties.LettuceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LettuceProperties.class)
public class LettuceConfiguration {



}
