package com.shiguang.service;

import com.shiguang.dto.WorkPageQueryDTO;
import com.shiguang.dto.WorkPublishDTO;
import com.shiguang.dto.NearbyWorksQueryDTO;
import com.shiguang.dto.ProfilePageQueryDTO;
import com.shiguang.dto.WorkUpdateDTO;
import com.shiguang.result.PageResult;
import com.shiguang.vo.FavoriteResultVO;
import com.shiguang.vo.WorkLikeResultVO;
import com.shiguang.vo.WorkVO;

public interface WorkService {
    /**
     * 分页查询帖子
     * @param workPageQueryDTO
     * @return
     */
    PageResult<WorkVO> pageQuery(WorkPageQueryDTO workPageQueryDTO);

    /**
     * 查询附近的作品（POST /api/works/nearby）。
     *
     * 以 (latitude, longitude) 为圆心、radiusKm 为半径（公里），返回已发布且带坐标的作品，
     * 按与圆心的距离由近到远排序。公开接口，无需登录。
     *
     * @param nearbyWorksQueryDTO 查询参数（中心经纬度、半径、分页）
     * @return 附近作品分页结果（含 distance 字段）
     */
    PageResult<WorkVO> pageNearby(NearbyWorksQueryDTO nearbyWorksQueryDTO);

    /**
     * 发布作品/保存草稿（POST /api/works）。
     *
     * categoryIds 随请求提交，服务端在同一事务内插入 works 主记录，
     * 并把分类写入 work_categories 关联表。
     *
     * @param workPublishDTO 发布请求体（status/title/.../categoryIds）
     * @return 创建完成的作品（含所属分类）
     */
    WorkVO insert(WorkPublishDTO workPublishDTO);

    /**
     * 分页查询帖子
     * @param id
     * @return
     */
    WorkVO getById(String id);

    /**
     * 修改作品（PUT /api/works/{id}）。
     *
     * 仅作者可改；标量字段 null=保持不变、列表字段 null=保持不变、[]=清空。
     * status 置 published 时校验标题非空且至少一个分类（取提交值或已有分类），
     * publishedAt 按最终状态重算。同事务更新 works 主记录并重建分类与标签关联。
     *
     * @param id   作品 ID
     * @param workUpdateDTO 修改请求体
     * @return 修改完成的作品（含所属分类与标签）
     */
    WorkVO update(String id, WorkUpdateDTO workUpdateDTO);

    /**
     * 点赞
     * @param id
     * @return
     */
    WorkLikeResultVO like(String id);

    /**
     * 取消点赞
     * @param id
     * @return
     */
    WorkLikeResultVO unlike(String id);

    /**
     * 收藏
     * @param id
     * @return
     */
    FavoriteResultVO favorite(String id);

    /**
     * 取消收藏
     * @param id
     * @return
     */
    FavoriteResultVO unfavorite(String id);

    /**
     * 我的收藏分页查询
     * @param favoritePageQueryDTO
     * @return
     */
    /**
     * 我的作品分页（GET /api/users/me/works）。
     *
     * @param profilePageQueryDTO current/pageSize 分页参数
     * @return 当前用户已发布作品分页结果（供个人主页“作品”Tab 展示）
     */
    PageResult<WorkVO> pageMyWorks(ProfilePageQueryDTO profilePageQueryDTO);

    /**
     * 我的作品管理分页（GET /api/users/me/manage-works）。
     *
     * @param profilePageQueryDTO current/pageSize 分页参数
     * @return 当前用户已发布及已下架作品分页结果（供“作品管理”页展示）
     */
    PageResult<WorkVO> pageMyManageWorks(ProfilePageQueryDTO profilePageQueryDTO);

    /**
     * 我的草稿分页（GET /api/users/me/drafts）。
     *
     * @param profilePageQueryDTO current/pageSize 分页参数
     * @return 当前用户草稿作品分页结果
     */
    PageResult<WorkVO> pageMyDrafts(ProfilePageQueryDTO profilePageQueryDTO);

    /**
     * 我的收藏分页（GET /api/users/me/favorites）。
     *
     * @param profilePageQueryDTO current/pageSize 分页参数
     * @return 当前用户收藏的已发布作品分页结果
     */
    PageResult<WorkVO> pageMyFavorites(ProfilePageQueryDTO profilePageQueryDTO);

    /**
     * 我的喜欢分页（GET /api/users/me/likes）。
     *
     * @param profilePageQueryDTO current/pageSize 分页参数
     * @return 当前用户点赞过的已发布作品分页结果
     */
    PageResult<WorkVO> pageMyLikes(ProfilePageQueryDTO profilePageQueryDTO);

    Integer total();

    /**
     * 删除作品
     */
    void delete(String id);

    /**
     * 下架作品（POST /api/works/{id}/offline）。
     *
     * 仅作者可操作，且作品当前必须为已发布。下架后公开列表不再展示，
     * 保留原发布时间与点赞/收藏数据，便于后续重新发布。
     *
     * @param id 作品 ID
     * @return 下架后的作品（含所属分类与标签）
     */
    WorkVO offline(String id);

    /**
     * 重新发布已下架作品（POST /api/works/{id}/republish）。
     *
     * 仅作者可操作，且作品当前必须为下架状态。重新发布时校验标题与分类完整性，
     * 并刷新发布时间使其回到瀑布流顶部。
     *
     * @param id 作品 ID
     * @return 重新发布后的作品（含所属分类与标签）
     */
    WorkVO republish(String id);
}
