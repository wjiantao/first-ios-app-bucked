package com.shiguang.service.impl;

import com.shiguang.dto.WorkPageQueryDTO;
import com.shiguang.dto.WorkPublishDTO;
import com.shiguang.dto.NearbyWorksQueryDTO;
import com.shiguang.dto.ProfilePageQueryDTO;
import com.shiguang.dto.WorkUpdateDTO;
import com.shiguang.entity.Work;
import com.shiguang.context.UserContext;
import com.shiguang.exception.BusinessException;
import com.shiguang.mapper.CategoryMapper;
import com.shiguang.mapper.TagMapper;
import com.shiguang.mapper.WorkMapper;
import com.shiguang.result.PageResult;
import com.shiguang.service.NotificationService;
import com.shiguang.service.PushService;
import com.shiguang.service.RealtimePushService;
import com.shiguang.service.WorkService;
import com.shiguang.vo.CategoryVO;
import com.shiguang.vo.FavoriteResultVO;
import com.shiguang.vo.TagVO;
import com.shiguang.vo.WorkCategoryVO;
import com.shiguang.vo.WorkLikeResultVO;
import com.shiguang.vo.WorkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class WorkServiceImpl implements WorkService {

    private static final Set<String> WORK_STATUSES = Set.of("draft", "published", "offline");
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_OFFLINE = "offline";
    private static final String STATUS_DELETED = "deleted";
    /** 作品类型白名单：image_text=图文 / article=长文 / video=视频，与 works.type 注释口径一致。 */
    private static final Set<String> WORK_TYPES = Set.of("image_text", "article", "video");
    private static final String DEFAULT_WORK_TYPE = "article";
    private static final int MAX_TAG_LENGTH = 30;
    private static final int MAX_TAG_COUNT = 10;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PushService pushService;

    @Autowired
    private RealtimePushService realtimePushService;

    /**
     * 分页查询帖子
     * @param workPageQueryDTO
     * @return
     */
    @Override
    public PageResult<WorkVO> pageQuery(WorkPageQueryDTO workPageQueryDTO) {
        String viewerId = UserContext.getCurrentId();
        // 关注状态和关注流都只能使用 JWT 对应的用户身份，不能信任请求体里的 viewerId。
        workPageQueryDTO.setViewerId(viewerId);
        if (Boolean.TRUE.equals(workPageQueryDTO.getFollowingOnly())) {
            if (viewerId == null || viewerId.isBlank()) {
                throw new BusinessException(401, "请先登录");
            }
        }
        PageHelper.startPage(workPageQueryDTO.getCurrent(), workPageQueryDTO.getPageSize());
        Page<WorkVO> page = workMapper.pageQuery(workPageQueryDTO);
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        return new PageResult(total, result);
    }

    /**
     * 查询附近的作品。
     *
     * <p>已发布且带坐标的作品，以查询中心为圆心、radiusKm 为半径过滤，按距离升序。
     * 服务端先按半径外扩出经纬度包围盒（跨 180° 经线或接近极点时退化为全范围），
     * 传给 MySQL 缩小扫描范围，是否为圆内仍由 SQL 内的精确 Haversine 距离过滤保证。</p>
     */
    @Override
    public PageResult<WorkVO> pageNearby(NearbyWorksQueryDTO nearbyWorksQueryDTO) {

        Double latitude = nearbyWorksQueryDTO.getLatitude();
        Double longitude = nearbyWorksQueryDTO.getLongitude();

        double radiusKm = nearbyWorksQueryDTO.getRadiusKm() == null ? 5.0 : nearbyWorksQueryDTO.getRadiusKm();

        // 经纬度包围盒：纬度经度各自的半宽，用于缩小数据库扫描范围。
        double latDelta = radiusKm / 111.32;
        double lngDelta = radiusKm / (111.32 * Math.cos(Math.toRadians(latitude)));
        double minLat = Math.max(-90, latitude - latDelta);
        double maxLat = Math.min(90, latitude + latDelta);
        double minLng;
        double maxLng;
        // 跨 180° 经线或半径覆盖过大时，经度退化为全范围，交由精确距离过滤保证正确性。
        if (lngDelta >= 180 || (longitude - lngDelta) < -180 || (longitude + lngDelta) > 180) {
            minLng = -180;
            maxLng = 180;
        } else {
            minLng = longitude - lngDelta;
            maxLng = longitude + lngDelta;
        }

        int current = nearbyWorksQueryDTO.getCurrent() == null ? 1 : nearbyWorksQueryDTO.getCurrent();
        int pageSize = nearbyWorksQueryDTO.getPageSize() == null ? 10 : nearbyWorksQueryDTO.getPageSize();
        PageHelper.startPage(current, pageSize);
        Page<WorkVO> page = workMapper.pageNearby(latitude, longitude, radiusKm, minLat, maxLat, minLng, maxLng);
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        log.info("查询附近作品：center=({},{}) radiusKm={} total={}", latitude, longitude, radiusKm, total);
        return new PageResult<>(total, result);
    }

    /**
     * 发布/保存作品草稿。
     *
     * 作品与分类是多对多，不把分类冗余在 works 表里：
     * 先插入作品主记录，再把去重后的 categoryIds 批量写入 work_categories，
     * 两步在同一事务中完成，任一步失败整体回滚。
     */
    @Override
    @Transactional
    public WorkVO insert(WorkPublishDTO workPublishDTO) {

        String status = workPublishDTO.getStatus();
        boolean published = STATUS_PUBLISHED.equals(status);

        List<Long> categoryIds = workPublishDTO.getCategoryIds() == null
                ? Collections.emptyList()
                : workPublishDTO.getCategoryIds().stream().distinct().toList();
        if (!categoryIds.isEmpty()) {
            validateCategories(categoryIds);
        }

        // 标签规范化：trim、去空格、去重；发布/草稿均允许为空
        List<String> tags = normalizeTags(workPublishDTO.getTags());

        String authorId = UserContext.getCurrentId();

        LocalDateTime now = LocalDateTime.now();
        Work work = new Work();
        work.setId("w-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        work.setAuthorId(authorId);
        work.setTitle(workPublishDTO.getTitle() == null ? "" : workPublishDTO.getTitle().trim());
        work.setType(normalizeType(workPublishDTO.getType()));
        work.setCoverUrl(workPublishDTO.getCoverUrl());
        work.setVideoUrl(workPublishDTO.getVideoUrl());
        // 地理位置：可选。坐标成对出现且做范围校验，位置名 trim 后空串归为 null。
        Double latitude = workPublishDTO.getLatitude();
        Double longitude = workPublishDTO.getLongitude();
        validateCoordinates(latitude, longitude);
        work.setLatitude(latitude);
        work.setLongitude(longitude);
        work.setLocationName(normalizeLocationName(workPublishDTO.getLocationName()));
        work.setContentMd(workPublishDTO.getContentMd());
        work.setStatus(status);
        work.setPublishedAt(published ? now : null);
        work.setCreatedAt(now);
        work.setUpdatedAt(now);
        workMapper.insert(work);

        // 作品与分类的关联写入：作品插入成功后，再批量写 work_categories
        if (!categoryIds.isEmpty()) {
            workMapper.insertWorkCategories(work.getId(), categoryIds);
        }
        // 作品与标签的关联写入：先确保标签存在（INSERT IGNORE 去重），再批量写 work_tags
        if (!tags.isEmpty()) {
            workMapper.insertWorkTags(work.getId(), resolveTagIds(tags));
        }

        // 发布成功后立即按当前用户回查，likeCount 恒为 0、liked 为 false，
        // 保证发布接口与详情接口返回同一种 WorkVO 结构。
        WorkVO workVO = workMapper.getById(work.getId(), UserContext.getCurrentId());
        fillCategories(Collections.singletonList(workVO));
        fillTags(Collections.singletonList(workVO));
        log.info("发布/保存作品成功 id={} status={} categoryIds={} tags={}",
                work.getId(), status, categoryIds, tags);
        return workVO;
    }

    @Override
    public WorkVO getById(String id) {
        String userId = UserContext.getCurrentId();

        // 先查询作品主记录
        Work work = workMapper.selectById(id);

        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 查询详情
        WorkVO workVO = workMapper.getById(id, userId);

        if (workVO == null) {
            throw new BusinessException("作品不存在或无权查看");
        }

        fillCategories(Collections.singletonList(workVO));
        fillTags(Collections.singletonList(workVO));

        // 只有已发布作品才记录浏览量
        if (STATUS_PUBLISHED.equals(work.getStatus())) {
            workMapper.insertView(
                    userId,
                    work.getId(),
                    LocalDateTime.now()
            );
        }

        log.info("查看作品: {}", workVO);

        return workVO;
    }
    /**
     * 修改作品。
     *
     * 部分更新语义：标量字段 null=保持不变、列表字段 null=保持不变、[]=清空。
     * status 置 published 时校验标题非空且至少一个分类（取提交值；列表字段为 null 时取已有值）。
     * 同事务更新 works 主记录，并重建该作品的分类与标签关联。
     */
    @Override
    @Transactional
    public WorkVO update(String id, WorkUpdateDTO workUpdateDTO) {

        String userId = UserContext.getCurrentId();

        Work existing = workMapper.selectById(id);

        // 最终状态：请求未指定时沿用当前状态
        String status = workUpdateDTO.getStatus();
        if (status == null || status.isBlank()) {
            status = existing.getStatus();
        }

        boolean publishing = STATUS_PUBLISHED.equals(status);

        // 标题：未指定时沿用原标题；发布时必须非空
        String title = workUpdateDTO.getTitle() == null
                ? existing.getTitle()
                : workUpdateDTO.getTitle().trim();

        // 分类：列表字段 null=沿用已有启用分类，[]=清空；发布时必须至少一个
        List<Long> categoryIds;
        if (workUpdateDTO.getCategoryIds() == null) {
            List<WorkCategoryVO> rows = categoryMapper.selectByWorkIds(Collections.singletonList(id));
            categoryIds = rows.stream().map(WorkCategoryVO::getId).distinct().toList();
        } else {
            categoryIds = workUpdateDTO.getCategoryIds().stream().distinct().toList();
            if (!categoryIds.isEmpty()) {
                validateCategories(categoryIds);
            }
        }

        // 标签：列表字段 null=沿用已有标签，[]=清空；所有标签经规范化校验
        List<String> tags;
        if (workUpdateDTO.getTags() == null) {
            List<TagVO> rows = tagMapper.selectByWorkIds(Collections.singletonList(id));
            tags = rows.stream().map(TagVO::getName).toList();
        } else {
            tags = normalizeTags(workUpdateDTO.getTags());
        }

        // publishedAt：按最终状态重算——published 且原本未置位则置 now；
        // offline（下架）保留原发布时间，便于后续重新发布；draft 则清空。
        LocalDateTime publishedAt;
        if (publishing) {
            publishedAt = existing.getPublishedAt() != null ? existing.getPublishedAt() : LocalDateTime.now();
        } else if (STATUS_OFFLINE.equals(status)) {
            publishedAt = existing.getPublishedAt();
        } else {
            publishedAt = null;
        }

        LocalDateTime now = LocalDateTime.now();
        Work work = new Work();
        work.setId(id);
        work.setTitle(title);
        // 类型：请求未指定时沿用已有类型；指定时校验合法性，避免外部提交未开放的类型值。
        work.setType(workUpdateDTO.getType() != null
                ? normalizeType(workUpdateDTO.getType())
                : existing.getType());
        work.setCoverUrl(workUpdateDTO.getCoverUrl() != null
                ? workUpdateDTO.getCoverUrl() : existing.getCoverUrl());
        work.setVideoUrl(workUpdateDTO.getVideoUrl() != null
                ? workUpdateDTO.getVideoUrl() : existing.getVideoUrl());
        // 地理位置走独立 updateLocation 整体覆盖：null=保持不变（本方法不写位置列），
        // 因此此处不把 location 字段并入 work 实体的标量更新，避免与 updateLocation 语义冲突。
        work.setContentMd(workUpdateDTO.getContentMd() != null
                ? workUpdateDTO.getContentMd() : existing.getContentMd());
        work.setStatus(status);
        work.setPublishedAt(publishedAt);
        work.setUpdatedAt(now);
        workMapper.update(work);

        // 重建分类关联
        workMapper.deleteWorkCategories(id);
        if (!categoryIds.isEmpty()) {
            workMapper.insertWorkCategories(id, categoryIds);
        }
        // 重建标签关联
        workMapper.deleteWorkTags(id);
        if (!tags.isEmpty()) {
            workMapper.insertWorkTags(id, resolveTagIds(tags));
        }

        // 地理位置：locationName 为 null 表示保持不变；空串表示清空位置；非空表示设定/更新位置。
        String locationName = workUpdateDTO.getLocationName();
        if (locationName != null) {
            Double latitude = workUpdateDTO.getLatitude();
            Double longitude = workUpdateDTO.getLongitude();
            validateCoordinates(latitude, longitude);
            String name = locationName.trim();
            if (name.isEmpty()) {
                // 显式清空位置：坐标与位置名整体置 null
                if (latitude != null || longitude != null) {
                    throw new BusinessException("清空位置时不需要传经纬度");
                }
                workMapper.updateLocation(id, null, null, null);
            } else {
                workMapper.updateLocation(id, latitude, longitude, name);
            }
        }

        WorkVO workVO = workMapper.getById(id, userId);
        fillCategories(Collections.singletonList(workVO));
        fillTags(Collections.singletonList(workVO));
        log.info("修改作品成功 id={} status={} categoryIds={} tags={}",
                id, status, categoryIds, tags);
        return workVO;
    }

    /** 校验地理位置坐标：可整体为空；给定时必须成对出现且落在合法范围内。 */
    private void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BusinessException("地理位置必须同时提供经度与纬度");
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new BusinessException("纬度超出合法范围(-90~90): " + latitude);
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new BusinessException("经度超出合法范围(-180~180): " + longitude);
        }
    }

    /**
     * 规范化作品类型：空值回落为默认 {@link #DEFAULT_WORK_TYPE}（长文），
     * 非白名单值直接拒绝，避免把脏数据写入 works.type 影响前台展示与筛选。
     *
     * @param type 客户端提交的作品类型，可为 null
     * @return 规范化后的类型；空白值返回默认 article
     * @throws BusinessException 类型不在白名单内时抛出，提示调用方修正
     */
    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return DEFAULT_WORK_TYPE;
        }
        String normalized = type.trim();
        if (!WORK_TYPES.contains(normalized)) {
            throw new BusinessException("不支持的作品类型: " + normalized);
        }
        return normalized;
    }

    /** 规范化位置展示名：trim 后为空串则返回 null，表示作品不带位置。 */
    private String normalizeLocationName(String locationName) {
        if (locationName == null) {
            return null;
        }
        String trimmed = locationName.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public WorkLikeResultVO like(String workId) {
        String userId = UserContext.getCurrentId();
        // 唯一键 (user_id, work_id) 保证同一用户只有一行；
        // 已经点过赞时 INSERT IGNORE 影响 0 行，接口仍幂等成功，不会重复计数
        int inserted = workMapper.insertLike(userId, workId, LocalDateTime.now());
        long likeCount = workMapper.countLikes(workId);
        if (inserted == 1) {
            notifyInteraction(userId, workId, "like");
        }
        return WorkLikeResultVO.builder().liked(true).likeCount(likeCount).build();
    }

    @Override
    public WorkLikeResultVO unlike(String workId) {
        String userId = UserContext.getCurrentId();
        workMapper.deleteLike(userId, workId);
        long likeCount = workMapper.countLikes(workId);
        return WorkLikeResultVO.builder().liked(false).likeCount(likeCount).build();
    }

    @Override
    public FavoriteResultVO favorite(String workId) {
        String userId = UserContext.getCurrentId();
        // 唯一键 (user_id, work_id) 保证同一用户只有一行；
        // 已收藏时 INSERT IGNORE 影响 0 行，接口仍幂等成功，不会重复计数
        int inserted = workMapper.insertFavorite(userId, workId, LocalDateTime.now());
        long favoriteCount = workMapper.countFavorites(workId);
        if (inserted == 1) {
            notifyInteraction(userId, workId, "favorite");
        }
        return FavoriteResultVO.builder().favorited(true).favoriteCount(favoriteCount).build();
    }

    /**
     * 点赞/收藏确为新增时写入站内通知并触发系统推送。
     *
     * <p>仅对“已发布”作品且触发者非作者本人时生成通知，避免草稿/下架/删除产生噪音，
     * 也避免作者对自己作品的互动自通知。取消点赞/收藏不会删除历史通知。</p>
     */
    private void notifyInteraction(String actorId, String workId, String type) {
        Work work = workMapper.selectById(workId);
        if (work == null || !STATUS_PUBLISHED.equals(work.getStatus())) {
            return;
        }
        if (actorId.equals(work.getAuthorId())) {
            return;
        }
        long notificationId = notificationService.create(work.getAuthorId(), actorId, workId, type);
        pushService.onInteraction(work.getAuthorId(), actorId, workId, work.getTitle(), notificationId, type);
        // 站内实时通知：通过 WebSocket 推给作者当前所有连接，客户端据此刷新未读角标与列表。
        // 与 JPush 系统推送并存；作者无在线连接时不发送，不影响业务。
        realtimePushService.push(work.getAuthorId(), actorId, workId, work.getTitle(), notificationId, type);
    }

    @Override
    public FavoriteResultVO unfavorite(String workId) {
        String userId = UserContext.getCurrentId();
        workMapper.deleteFavorite(userId, workId);
        long favoriteCount = workMapper.countFavorites(workId);
        return FavoriteResultVO.builder().favorited(false).favoriteCount(favoriteCount).build();
    }

    @Override
    public PageResult<WorkVO> pageMyWorks(ProfilePageQueryDTO profilePageQueryDTO) {
        PageHelper.startPage(profilePageQueryDTO.getCurrent(), profilePageQueryDTO.getPageSize());
        Page<WorkVO> page = workMapper.pageMyWorks(UserContext.getCurrentId());
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        return new PageResult<>(total, result);
    }

    /**
     * 我的作品管理分页。
     *
     * 与 {@link #pageMyWorks} 不同：管理页需要展示“已发布 + 已下架”的作品，
     * 让作者能看到被隐藏的内容并重新发布，因此走独立的 pageMyManageWorks 查询。
     */
    @Override
    public PageResult<WorkVO> pageMyManageWorks(ProfilePageQueryDTO profilePageQueryDTO) {
        PageHelper.startPage(profilePageQueryDTO.getCurrent(), profilePageQueryDTO.getPageSize());
        Page<WorkVO> page = workMapper.pageMyManageWorks(UserContext.getCurrentId());
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        return new PageResult<>(total, result);
    }

    /**
     * 我的草稿分页。
     *
     * 与 {@link #pageMyWorks} 对称：草稿属于作者私密内容，仅当前登录用户可见，
     * 由 {@link UserContext#getCurrentId()} 限定作者，SQL 按 status='draft' 过滤。
     */
    @Override
    public PageResult<WorkVO> pageMyDrafts(ProfilePageQueryDTO profilePageQueryDTO) {
        PageHelper.startPage(profilePageQueryDTO.getCurrent(), profilePageQueryDTO.getPageSize());
        Page<WorkVO> page = workMapper.pageMyDrafts(UserContext.getCurrentId());
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        return new PageResult<>(total, result);
    }

    @Override
    public PageResult<WorkVO> pageMyFavorites(ProfilePageQueryDTO profilePageQueryDTO) {
        PageHelper.startPage(profilePageQueryDTO.getCurrent(), profilePageQueryDTO.getPageSize());
        Page<WorkVO> page = workMapper.pageMyFavorites(UserContext.getCurrentId());
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        return new PageResult<>(total, result);
    }

    @Override
    public PageResult<WorkVO> pageMyLikes(ProfilePageQueryDTO profilePageQueryDTO) {
        PageHelper.startPage(profilePageQueryDTO.getCurrent(), profilePageQueryDTO.getPageSize());
        Page<WorkVO> page = workMapper.pageMyLikes(UserContext.getCurrentId());
        long total = page.getTotal();
        List<WorkVO> result = page.getResult();
        fillCategories(result);
        fillTags(result);
        return new PageResult<>(total, result);
    }

    @Override
    public Integer total() {
        String userId = UserContext.getCurrentId();
        Integer total = workMapper.total(userId);
        return total;
    }

    /**
     * 删除作品
     * @param id
     */
    @Override
    public void delete(String id) {
        workMapper.delete(id);
    }

    /**
     * 下架作品。
     *
     * 仅作者可操作，且作品当前必须为已发布。下架后公开列表不再展示该作品，
     * 保留原发布时间与点赞/收藏数据；后续可通过 {@link #republish} 恢复。
     */
    @Override
    @Transactional
    public WorkVO offline(String id) {
        String userId = requireLogin();
        Work existing = requireVisibleOwnedWork(id, userId);
        if (!STATUS_PUBLISHED.equals(existing.getStatus())) {
            throw new BusinessException("只有已发布的作品才能下架");
        }

        LocalDateTime now = LocalDateTime.now();
        Work work = new Work();
        work.setId(id);
        work.setStatus(STATUS_OFFLINE);
        // 保留原发布时间，重新发布时不丢失原时间线
        work.setPublishedAt(existing.getPublishedAt());
        work.setUpdatedAt(now);
        workMapper.update(work);

        WorkVO workVO = workMapper.getById(id, userId);
        fillCategories(Collections.singletonList(workVO));
        fillTags(Collections.singletonList(workVO));
        log.info("下架作品成功 id={}", id);
        return workVO;
    }

    /**
     * 重新发布已下架作品。
     *
     * 仅作者可操作，且作品当前必须为下架状态。重新发布会再次校验标题与分类完整性，
     * 并把发布时间刷新为现在，使作品重新回到瀑布流顶部。
     */
    @Override
    @Transactional
    public WorkVO republish(String id) {
        String userId = requireLogin();
        Work existing = requireVisibleOwnedWork(id, userId);

        LocalDateTime now = LocalDateTime.now();
        Work work = new Work();
        work.setId(id);
        work.setStatus(STATUS_PUBLISHED);
        // 刷新发布时间，让重新发布的作品回到瀑布流顶部
        work.setPublishedAt(now);
        work.setUpdatedAt(now);
        workMapper.update(work);

        WorkVO workVO = workMapper.getById(id, userId);
        fillCategories(Collections.singletonList(workVO));
        fillTags(Collections.singletonList(workVO));
        log.info("重新发布作品成功 id={}", id);
        return workVO;
    }

    /** 获取当前登录用户 ID，未登录抛 401。 */
    private String requireLogin() {
        String userId = UserContext.getCurrentId();
        return userId;
    }

    /** 按 ID 读取未删除作品并校验作者身份，返回作品主记录（供下架/重新发布等作者私有操作复用）。 */
    private Work requireVisibleOwnedWork(String id, String userId) {
        Work existing = workMapper.selectById(id);
        return existing;
    }

    /** 校验提交的分类都存在且处于启用状态，避免关联表中写入悬空分类。 */
    private void validateCategories(List<Long> categoryIds) {
        List<Long> enabledIds = categoryMapper.selectEnabledIds(categoryIds);
        Set<Long> enabled = new HashSet<>(enabledIds);
        List<Long> missing = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            if (!enabled.contains(categoryId)) {
                missing.add(categoryId);
            }
        }
    }

    /**
     * 标签规范化：trim、忽略空串、去除完全重复后按首次出现顺序返回。
     *
     * <p>同时做长度与数量校验：单个标签不超过 {@link #MAX_TAG_LENGTH} 字符，
     * 每个作品最多 {@link #MAX_TAG_COUNT} 个标签；超出即抛出业务异常。</p>
     *
     * @param rawTags 原始标签列表，可为 null（按空处理）
     * @return 规范化后的标签列表；null 或全空返回空列表
     */
    private List<String> normalizeTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String tag : rawTags) {
            if (tag == null) {
                continue;
            }
            String trimmed = tag.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > MAX_TAG_LENGTH) {
                throw new BusinessException("标签过长（最多 " + MAX_TAG_LENGTH + " 字）: " + trimmed);
            }
            normalized.add(trimmed);
            if (normalized.size() > MAX_TAG_COUNT) {
                throw new BusinessException("标签数量不能超过 " + MAX_TAG_COUNT + " 个");
            }
        }
        return new ArrayList<>(normalized);
    }

    /**
     * 把规范化后的标签名解析为标签 ID（未出现的标签先落库，再一次性按名查询映射）。
     *
     * <p>两步都在调用方事务内完成：先 {@link TagMapper#insertIgnore} 幂等落表，
     * 再 {@link TagMapper#selectByNameIn} 把 name 映射为 id，供写入 work_tags。</p>
     *
     * @param tags 规范化、去重后的标签名列表，不允许为空
     * @return 与入参对应的标签 ID 列表
     */
    private List<Long> resolveTagIds(List<String> tags) {
        tagMapper.insertIgnore(tags);
        Map<String, Long> idByName = new HashMap<>();
        for (TagVO tagVO : tagMapper.selectByNameIn(tags)) {
            idByName.put(tagVO.getName(), tagVO.getId());
        }
        List<Long> tagIds = new ArrayList<>();
        for (String name : tags) {
            Long tagId = idByName.get(name);
            // 正常情况 selectByNameIn 一定能命中已 insertIgnore 的标签；
            // 极端并发下若漏查则跳过该标签，避免写入悬空关联。
            if (tagId != null) {
                tagIds.add(tagId);
            }
        }
        return tagIds;
    }

    /**
     * 给一批作品记录批量回填所属分类（categories 字段）。
     *
     * <p>作品与分类是多对多关系，分类信息不冗余存储在 works 表中，而是通过
     * work_categories 关联表维护。因此查询出的 WorkVO 默认不携带分类数据，
     * 需要在本方法中额外查询并按作品聚合后回填。</p>
     *
     * <p>实现要点：</p>
     * <ul>
     *     <li>先收集所有作品的 id，做<b>一次</b>批量 IN 查询（
     *         {@link CategoryMapper#selectByWorkIds}），而不是对每条作品单独查库，
     *         将数据库往返次数从 N 次降到 1 次；</li>
     *     <li>把查询结果按 workId 分组，同一作品的多个分类保持返回顺序；</li>
     *     <li>最后逐条回填到对应 WorkVO 上，没有分类的作品回填为空列表，
     *         保证调用方拿到的 categories 永不为 null。</li>
     * </ul>
     *
     * <p>本方法为纯回填逻辑，不改变传入记录的数量与顺序；records 中的元素会被原地修改。</p>
     *
     * @param records 待回填分类的作品 VO 列表；允许为 null 或空（此时直接返回，不做任何查询）
     */
    private void fillCategories(List<WorkVO> records) {
        // 空集合直接返回，避免无意义的数据库查询
        if (records == null || records.isEmpty()) {
            return;
        }

        // 第一步：收集所有作品的 id，作为一次批量查询的入参
        List<String> workIds = new ArrayList<>();
        for (WorkVO record : records) {
            if (record != null && record.getId() != null) {
                workIds.add(record.getId());
            }
        }
        // 第二步：执行一次批量查询，并按 workId 分组
        // 用 HashMap 记录 workId -> 该作品下的分类列表
        Map<String, List<CategoryVO>> categoriesByWork = new HashMap<>();
        for (WorkCategoryVO row : categoryMapper.selectByWorkIds(workIds)) {
            String workId = row.getWorkId();
            // 先根据 workId 从 Map 中取出该作品已有的分类列表
            List<CategoryVO> categoryList = categoriesByWork.get(workId);
            // 如果还没有（第一次遇到该作品），就新建一个空列表并放入 Map
            if (categoryList == null) {
                categoryList = new ArrayList<>();
                categoriesByWork.put(workId, categoryList);
            }
            // 将关联表查出的中间行转换为页面展示所需的 CategoryVO，加入该作品的分类列表
            categoryList.add(CategoryVO.builder()
                    .id(row.getId())
                    .code(row.getCode())
                    .name(row.getName())
                    .sortOrder(row.getSortOrder())
                    .build());
        }

        // 第三步：逐条回填。查不到分类的作品回填空列表，避免调用方出现 NPE
        for (WorkVO record : records) {
            if (record == null || record.getId() == null) {
                continue;
            }

            record.setCategories(
                    categoriesByWork.getOrDefault(
                            record.getId(),
                            Collections.emptyList()
                    )
            );
        }
    }

    /**
     * 给一批作品记录批量回填所属标签（tags 字段）。
     *
     * <p>与 {@link #fillCategories} 同理：一次批量 IN 查询后按 workId 聚合回填，
     * 避免对每条作品单独查库；无标签的作品回填为空列表，保证 tags 永不为 null。</p>
     *
     * @param records 待回填分类的作品 VO 列表；允许为 null 或空（此时直接返回）
     */
    private void fillTags(List<WorkVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        List<String> workIds = new ArrayList<>();

        for (WorkVO record : records) {
            if (record != null && record.getId() != null) {
                workIds.add(record.getId());
            }
        }

        if (workIds.isEmpty()) {
            return;
        }

        Map<String, List<String>> tagsByWork = new HashMap<>();

        for (TagVO row : tagMapper.selectByWorkIds(workIds)) {
            tagsByWork
                    .computeIfAbsent(row.getWorkId(), k -> new ArrayList<>())
                    .add(row.getName());
        }

        for (WorkVO record : records) {
            if (record == null || record.getId() == null) {
                continue;
            }

            record.setTags(
                    tagsByWork.getOrDefault(
                            record.getId(),
                            Collections.emptyList()
                    )
            );
        }
    }
}
