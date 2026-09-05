package com.shiguang.mapper;

import com.github.pagehelper.Page;
import com.shiguang.dto.NearbyWorksQueryDTO;
import com.shiguang.dto.WorkPageQueryDTO;
import com.shiguang.entity.Work;
import com.shiguang.vo.WorkVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作品表数据访问接口。
 *
 * 与 UserMapper 一致：SQL 收敛在 resources/mapper/WorkMapper.xml 中。
 * pageQuery 需配合 PageHelper.startPage 使用，返回的即分页后的 Page&lt;WorkVO&gt;。
 */
public interface WorkMapper {
    Page<WorkVO> pageQuery(WorkPageQueryDTO workPageQueryDTO);

    /**
     * 分页查询附近的作品（配套 PageHelper.startPage 使用）。
     *
     * <p>以 (latitude, longitude) 为圆心、radiusKm 为半径，返回已发布且带坐标的作品，
     * 按与圆心的距离（公里，Haversine）由近到远排序。minLat/maxLat/minLng/maxLng 为
     * 服务端按半径外扩得到的经纬度包围盒，用于缩小扫描范围；是否为圆内仍由 SQL 内
     * 的精确 Haversine 距离 {@code <= radiusKm} 过滤保证。</p>
     *
     * @param latitude  中心纬度
     * @param longitude 中心经度
     * @param radiusKm  半径（公里）
     * @param minLat    包围盒最小纬度
     * @param maxLat    包围盒最大纬度
     * @param minLng    包围盒最小经度
     * @param maxLng    包围盒最大经度
     * @return 已发布作品分页（含 distance 字段），配合 PageHelper.startPage 使用
     */
    Page<WorkVO> pageNearby(@Param("latitude") Double latitude,
                            @Param("longitude") Double longitude,
                            @Param("radiusKm") Double radiusKm,
                            @Param("minLat") Double minLat,
                            @Param("maxLat") Double maxLat,
                            @Param("minLng") Double minLng,
                            @Param("maxLng") Double maxLng);

    /**
     * 插入一条作品记录。
     *
     * @param work 由服务端补全 id/authorId/publishedAt/createdAt/updatedAt 的作品实体
     */
    void insert(Work work);

    /**
     * 批量写入作品-分类关联行（work_categories）。
     *
     * @param workId      作品 ID
     * @param categoryIds 去重后的分类 ID 列表，不允许为空
     */
    void insertWorkCategories(@Param("workId") String workId,
                              @Param("categoryIds") List<Long> categoryIds);

    /**
     * 删除作品的全部分类关联（编辑时重建 work_categories 前调用）。
     *
     * @param workId 作品 ID
     */
    void deleteWorkCategories(@Param("workId") String workId);

    /**
     * 批量写入作品-标签关联行（work_tags）。
     *
     * @param workId 作品 ID
     * @param tagIds 已解析的标签 ID 列表
     */
    void insertWorkTags(@Param("workId") String workId,
                        @Param("tagIds") List<Long> tagIds);

    /**
     * 删除作品的全部标签关联（编辑时重建 work_tags 前调用）。
     *
     * @param workId 作品 ID
     */
    void deleteWorkTags(@Param("workId") String workId);

    /**
     * 按 ID 查询作品（不含已软删除记录），供发布后回查完整作品。
     *
     * @param id     作品 ID
     * @param userId 当前登录用户 ID，用于回填“当前用户是否已点赞”
     * @return 作品 VO，不存在返回 null
     */
    WorkVO getById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 按 ID 查询作品主记录（含 author_id/status/published_at），
     * 供编辑接口校验作者身份与读取当前状态。
     *
     * @param id 作品 ID
     * @return 作品实体，不存在返回 null
     */
    Work selectById(@Param("id") String id);

    /**
     * 局部更新作品主记录：标量字段传 null 保持不变，
     * published_at/updated_at 始终按服务端计算值写入。
     *
     * @param work 由服务端合并后的作品实体
     */
    void update(Work work);

    /**
     * 单独更新作品的地理位置（纬度/经度/位置名）。
     *
     * <p>与 {@link #update} 的“单个标量字段 null=保持不变”不同，这里把地理位置视为一个整体，
     * 一次性写入三个字段，三个字段均可能为 null（传 null 即清除位置），用于“新增/更新/清空位置”
     * 三种语义。调用方需自行区分“保持不变”（不调用本方法）与“清空”（三个参数都传 null）。</p>
     *
     * @param id           作品 ID
     * @param latitude     纬度，可空（清除位置时为 null）
     * @param longitude    经度，可空（清除位置时为 null）
     * @param locationName 位置展示名，可空（清除位置时为 null）
     */
    void updateLocation(@Param("id") String id,
                        @Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("locationName") String locationName);

    /**
     * 点赞（INSERT IGNORE 幂等）。
     *
     * @param userId    触发者用户 ID
     * @param workId    被点赞作品 ID
     * @param createdAt 点赞时间
     * @return 影响行数：1 表示本次确实新增了一条点赞，0 表示此前已点过赞（未重复计数）
     */
    int insertLike(String userId, String workId, LocalDateTime createdAt);

    /**
     * 记录一次作品浏览（GET /api/works/{id} 成功打开已发布作品时调用）。
     *
     * 用于创作中心「数据概览」统计累计与今日新增浏览量；
     * 是否落库（作者本人、未发布作品除外）由服务层判断，这里的 MySQL 不设任何去重。
     *
     * @param viewerId  浏览者用户 ID
     * @param workId    被浏览作品 ID
     * @param createdAt 浏览时间（服务端本地时区）
     */
    void insertView(String viewerId, String workId, LocalDateTime createdAt);

    /**
     * 获取点赞数量
     * @param workId
     * @return
     */
    long countLikes(String workId);

    void deleteLike(String userId, String workId);

    /**
     * 收藏（INSERT IGNORE 幂等）。
     *
     * @param userId    触发者用户 ID
     * @param workId    被收藏作品 ID
     * @param createdAt 收藏时间
     * @return 影响行数：1 表示本次确实新增了一条收藏，0 表示此前已收藏过（未重复计数）
     */
    int insertFavorite(String userId, String workId, LocalDateTime createdAt);

    void deleteFavorite(String userId, String workId);

    long countFavorites(String workId);

    /**
     * 当前用户已发布作品分页（个人主页“作品”Tab）。
     *
     * @param userId 当前登录用户 ID
     * @return 已发布作品分页，配合 PageHelper.startPage 使用
     */
    Page<WorkVO> pageMyWorks(@Param("userId") String userId);

    /**
     * 当前用户作品管理列表分页（个人主页“作品管理”页）。
     *
     * <p>与 {@link #pageMyWorks} 不同：管理页需要展示“已发布 + 已下架”的作品，
     * 以便作者识别被隐藏的内容并重新发布；草稿仍由 {@link #pageMyDrafts} 单独处理。
     * 按 author_id = userId 且 status IN ('published','offline') 过滤。</p>
     *
     * @param userId 当前登录用户 ID
     * @return 分页结果，配合 PageHelper.startPage 使用
     */
    Page<WorkVO> pageMyManageWorks(@Param("userId") String userId);

    /**
     * 当前用户草稿作品分页（个人主页"草稿"Tab）。
     *
     * <p>草稿属于作者的私密内容，只能作者本人查看；列表不对外公开，
     * 因此按 author_id = userId 且 status = 'draft' 过滤。</p>
     *
     * @param userId 当前登录用户 ID
     * @return 草稿作品分页，配合 PageHelper.startPage 使用
     */
    Page<WorkVO> pageMyDrafts(@Param("userId") String userId);

    /**
     * 当前用户收藏作品分页（个人主页"收藏"Tab）。
     *
     * @param userId 当前登录用户 ID
     * @return 收藏的已发布作品分页，配合 PageHelper.startPage 使用
     */
    Page<WorkVO> pageMyFavorites(@Param("userId") String userId);

    /**
     * 当前用户点赞作品分页（个人主页“喜欢”Tab）。
     *
     * @param userId 当前登录用户 ID
     * @return 点赞过的已发布作品分页，配合 PageHelper.startPage 使用
     */
    Page<WorkVO> pageMyLikes(@Param("userId") String userId);

    /**
     * 获取作品数量
     * @param userId
     * @return
     */
    @Select("SELECT COUNT(*) FROM works WHERE author_id = #{userId}")
    Integer total(String userId);

    /**
     * 删除作品
     * @param id
     */
    @Delete("DELETE FROM works where id = #{id}")
    void delete(String id);
}
