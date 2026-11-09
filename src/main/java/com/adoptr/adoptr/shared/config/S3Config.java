package com.adoptr.adoptr.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "app.images.storage", havingValue = "s3", matchIfMissing = true)
public class S3Config {

    /** Las credenciales se resuelven recién al usarlo: sin AWS la app arranca y solo fallan las subidas. */
    @Bean(destroyMethod = "close")
    public S3Client s3Client(@Value("${aws.region}") String region) {
        return S3Client.builder().region(Region.of(region)).build();
    }
}
