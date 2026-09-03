package com.shiguang.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 作品-标签查询结果行（Mapper 内部使用，不分页返回给客户端）。
 *
 * 用于两种场景：
 * - 按标签名集合查询已存在的标签：{@link com.shiguang.mapper.TagMapper#selectByNameIn}，携带 id/name；
 * - 按作品 ID 集合批量查询其标签：{@link com.shiguang.mapper.TagMapper#selectByWorkIds}，
 *   workId 标记标签归属于哪条作品，供批量回填 WorkVO.tags。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVO {

    /** 作品 ID，批量回填场景使用。 */
    private String workId;

    private Long id;

    private String name;
}
