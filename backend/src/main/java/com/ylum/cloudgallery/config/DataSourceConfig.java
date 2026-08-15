package com.ylum.cloudgallery.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MySQL 业务库底层物理数据源配置。
 *
 * <p>显式声明 MySQL {@link DataSource} bean。原因是：一旦容器里存在自定义
 * {@link DataSource} bean，Spring Boot 的 {@code DataSourceAutoConfiguration} 会因
 * {@code @ConditionalOnMissingBean(DataSource.class)} 跳过主数据源自动配置。</p>
 *
 * <p>注意：MySQL 物理数据源（{@code mysqlDataSource}）不再作为主数据源，而是由
 * {@code ShardingSphereConfig} 包装为分片数据源后作为主数据源（{@code @Primary}）供 MyBatis-Plus
 * 使用。</p>
 */
@Configuration
public class DataSourceConfig {

    // ==================== MySQL 底层物理数据源 ====================

    /**
     * 绑定 {@code spring.datasource} 到 {@link DataSourceProperties}。
     *
     * @return MySQL 数据源配置属性
     */
    @Bean("mysqlDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * MySQL 业务库底层物理数据源（供 ShardingSphere 包装，非主数据源）。
     *
     * <p>主数据源由 {@code ShardingSphereConfig} 中的 {@code shardingSphereDataSource} 提供，
     * MyBatis-Plus 实际使用 ShardingSphere 包装后的分片数据源。</p>
     *
     * @param properties MySQL 数据源配置属性
     * @return MySQL 底层物理数据源
     */
    @Bean("mysqlDataSource")
    public DataSource mysqlDataSource(@Qualifier("mysqlDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
