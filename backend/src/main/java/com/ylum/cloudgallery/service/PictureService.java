package com.ylum.cloudgallery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.ylum.cloudgallery.model.dto.PictureEditRequest;
import com.ylum.cloudgallery.model.dto.PictureQueryRequest;
import com.ylum.cloudgallery.model.dto.PictureSearchByPictureRequest;
import com.ylum.cloudgallery.model.dto.PictureUploadByBatchRequest;
import com.ylum.cloudgallery.model.dto.PictureUploadRequest;
import com.ylum.cloudgallery.model.entity.Picture;
import com.ylum.cloudgallery.model.vo.PictureVO;

import java.util.List;

/**
 * 图片服务接口，定义上传 / 分页查询 / 详情 / 编辑 / 删除 / 批量抓取能力。
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片（本地文件或 URL），上传到 COS 并落库。
     *
     * @param request 上传请求参数
     * @return 图片视图对象
     */
    PictureVO uploadPicture(PictureUploadRequest request);

    /**
     * 分页查询图片。
     *
     * @param request 分页查询请求参数
     * @return 图片分页结果
     */
    Page<PictureVO> listPictureByPage(PictureQueryRequest request);

    /**
     * 查询图片详情。
     *
     * @param id 图片 ID
     * @return 图片视图对象
     */
    PictureVO getPictureVOById(long id);

    /**
     * 编辑图片（改名 + 移动空间）。
     *
     * @param request 编辑请求参数
     */
    void editPicture(PictureEditRequest request);

    /**
     * 删除图片（逻辑删除 MySQL + 删除 COS 对象）。
     *
     * @param id 图片 ID
     */
    void deletePicture(long id);

    /**
     * 批量抓取上传（Jsoup 爬取必应图片搜索结果）。
     *
     * @param request 批量抓取请求参数
     * @return 上传成功的图片列表
     */
    List<PictureVO> uploadPictureByBatch(PictureUploadByBatchRequest request);

    /**
     * 以图搜图：提取查询图特征，检索相似图片并返回详情。
     *
     * @param request 以图搜图请求参数
     * @return 相似图片视图对象列表（按相似度降序）
     */
    List<PictureVO> searchPictureByPicture(PictureSearchByPictureRequest request);

    /**
     * 按颜色搜索：以目标主色调为基准，用 RGB 欧氏距离排序返回相近图片。
     *
     * @param picColor 目标主色调（十六进制，如 0xe00000）
     * @param spaceId  空间 ID（空 = 公共图库）
     * @param topN     返回数量
     * @return 相近图片视图对象列表（按颜色距离升序）
     */
    List<PictureVO> searchPictureByColor(String picColor, Long spaceId, int topN);
}
