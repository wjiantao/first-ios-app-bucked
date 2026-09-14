package com.shiguang.service;

import com.shiguang.dto.ProfilePageQueryDTO;
import com.shiguang.result.PageResult;
import com.shiguang.vo.NotificationVO;

/**
 * 站内通知服务。
 *
 * 供点赞/收藏/关注在确认“本次确为新增”后写入通知，并支撑消息中心列表、未读数、已读操作。
 * 所有查询/写操作均以当前登录用户（{@code UserContext}）为接收者，无需在参数里透传 userId。
 */
public interface NotificationService {

    /**
     * 写入一条通知。
     *
     * <p>由 {@code WorkService} 在点赞/收藏确为新增时调用；调用方负责先判断
     * 作品已发布且触发者不是作者本人；关注通知的 workId 可以为空。</p>
     *
     * @param recipientId 接收者（作品作者）用户 ID
     * @param actorId     触发者（点赞/收藏的人）用户 ID
     * @param workId      被互动的作品 ID
     * @param type        通知类型：like / favorite / follow
     * @return 新建通知的 ID
     */
    long create(String recipientId, String actorId, String workId, String type);

    /** 当前用户通知分页（倒序，PageHelper）。 */
    PageResult<NotificationVO> page(ProfilePageQueryDTO profilePageQueryDTO);

    /** 当前用户未读通知数（供角标展示）。 */
    long unreadCount();

    /** 把当前用户所有未读通知置为已读，返回处理条数。 */
    int markAllRead();

    /** 把当前用户指定通知置为已读，返回影响行数（0 表示不存在或不属于当前用户）。 */
    int markRead(Long id);
}
