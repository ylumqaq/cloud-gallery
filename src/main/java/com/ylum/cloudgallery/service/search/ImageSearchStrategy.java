package com.ylum.cloudgallery.service.search;

import com.ylum.cloudgallery.manager.upload.model.UploadPictureResult;
import com.ylum.cloudgallery.model.entity.Picture;

import java.io.File;
import java.util.List;

/**
 * 以图搜图策略接口（策略模式）。
 *
 * <p>将「向量提取 + 相似度检索」抽象为可切换的策略，屏蔽底层实现差异：</p>
 * <ul>
 *     <li>{@code pg}：本地提取特征向量并写入自建 pgvector，检索在应用侧完成；</li>
 *     <li>{@code ci}：直接使用腾讯云数据万象 CI 的托管图片检索能力，不做中间向量提取。</li>
 * </ul>
 *
 * <p>通过 {@code image-search.strategy} 配置项切换具体实现。</p>
 */
public interface ImageSearchStrategy {

    /**
     * 策略名称（与配置项 {@code image-search.strategy} 对应）。
     *
     * @return 策略名称
     */
    String name();

    /**
     * 图片上传成功、元数据落库后的回调：负责向量写入（pg）或 CI 图库入库（ci）。
     *
     * <p>本方法执行失败不应影响图片上传主流程（最终一致），实现内部需自行捕获异常。</p>
     *
     * @param picture 已落库的图片实体
     * @param result  上传产物（含 COS 对象键）
     */
    void onUpload(Picture picture, UploadPictureResult result);

    /**
     * 图片删除后的回调：负责清理向量（pg 双删）或 CI 图库出库（ci）。
     *
     * <p>本方法执行失败不应影响图片删除主流程（最终一致），实现内部需自行捕获异常。</p>
     *
     * @param picture 待删除的图片实体
     */
    void onDelete(Picture picture);

    /**
     * 以图搜图：根据查询图片返回相似图片 ID 列表（按相似度降序）。
     *
     * @param queryFile 查询图片本地文件
     * @param spaceId   限定空间 ID（null 表示仅公共图库）
     * @param topK      返回数量上限
     * @return 相似图片 ID 列表
     */
    List<Long> search(File queryFile, Long spaceId, int topK);
}
