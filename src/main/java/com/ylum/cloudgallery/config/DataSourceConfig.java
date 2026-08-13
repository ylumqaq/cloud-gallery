package com.ylum.cloudgallery.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 双数据源配置：MySQL 业务库底层物理数据源 + PostgreSQL 向量库。
 *
 * <p>显式声明 MySQL 与 PG 两个 {@link DataSource} bean。原因是：一旦容器里存在自定义
 * {@link DataSource} bean，Spring Boot 的 {@code DataSourceAutoConfiguration} 会因
 * {@code @ConditionalOnMissingBean(DataSource.class)} 跳过主数据源自动配置，导致 MyBatis 误连
 * PostgreSQL。因此这里把两个数据源都显式声明出来。</p>
 *
 * <p>注意：MySQL 物理数据源（{@code mysqlDataSource}）不再作为主数据源，而是由
 * {@code ShardingSphereConfig} 包装为分片数据源后作为主数据源（{@code @Primary}）供 MyBatis-Plus
 * 使用；PG 数据源作为非主数据源仅提供给 pgvector 向量表使用。</p>
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

    // ==================== PostgreSQL 向量库数据源 ====================

    /**
     * 绑定 {@code pg.datasource} 到 {@link DataSourceProperties}。
     *
     * @return PostgreSQL 数据源配置属性
     */
    @Bean("pgDataSourceProperties")
    @ConfigurationProperties(prefix = "pg.datasource")
    public DataSourceProperties pgDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * PostgreSQL 向量库数据源（非主数据源，供 pgvector 向量表使用）。
     *
     * @param properties PostgreSQL 数据源配置属性
     * @return PostgreSQL 数据源
     */
    @Bean("pgDataSource")
    public DataSource pgDataSource(@Qualifier("pgDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * 操作 PostgreSQL 向量库的 JdbcTemplate（bean 名称 {@code pgJdbcTemplate}）。
     *
     * @param dataSource PostgreSQL 数据源（限定 {@code pgDataSource}）
     * @return 指向 PostgreSQL 的 JdbcTemplate
     */
    @Bean("pgJdbcTemplate")
    public JdbcTemplate pgJdbcTemplate(@Qualifier("pgDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
