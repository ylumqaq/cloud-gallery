package com.ylum.cloudgallery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylum.cloudgallery.model.entity.User;

/**
 * 用户 Mapper，继承 MyBatis-Plus {@link BaseMapper} 获得基础 CRUD 能力。
 *
 * <p>由启动类 {@code @MapperScan("com.ylum.cloudgallery.mapper")} 统一扫描，
 * 因此无需在此重复标注 {@code @Mapper}。</p>
 */
public interface UserMapper extends BaseMapper<User> {
}
