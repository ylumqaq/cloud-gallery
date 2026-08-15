package com.ylum.cloudgallery.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 客户端配置。
 *
 * <p>读取 {@code application.yml} 中的 {@code cos.*} 配置并初始化 {@link COSClient}。
 * 构造 COSClient 仅设置凭证与地域，不发起网络请求，因此配置为占位符时不影响应用启动，
 * 实际调用上传/处理接口时才需要真实密钥。</p>
 */
@Configuration
@ConfigurationProperties(prefix = "cos")
@Data
public class CosClientConfig {

    /** 腾讯云 API 密钥 SecretId */
    private String secretId;

    /** 腾讯云 API 密钥 SecretKey */
    private String secretKey;

    /** 存储桶名称 */
    private String bucket;

    /** 地域，如 ap-guangzhou */
    private String region;

    /**
     * 初始化 COS 客户端。
     *
     * @return COSClient 实例
     */
    @Bean
    public COSClient cosClient() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(credentials, clientConfig);
    }
}
