package org.example.storemanager.shared.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud_name:ddjd3bkh2}") private String cloudName;
    @Value("${cloudinary.api_key:594541298539529}") private String apiKey;
    @Value("${cloudinary.api_secret:9RZap99fEpt7mm_jf7z0FZMNUik}") private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }
}
