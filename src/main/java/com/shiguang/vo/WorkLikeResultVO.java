package com.shiguang.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLikeResultVO {
    /** 操作后是否处于点赞状态 */
    private boolean liked;
    /** 操作后的点赞总数 */
    private long likeCount;
}
