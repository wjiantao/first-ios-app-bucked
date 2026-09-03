package com.shiguang.mapper;

import com.shiguang.vo.CategoryVO;
import com.shiguang.vo.WorkCategoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表数据访问接口。
 *
 * 与 UserMapper 一致：SQL 收敛在 resources/mapper/CategoryMapper.xml 中。
 */
public interface CategoryMapper {

    /**
     * 查询启用中的全部分类，按 sort_order 升序。
     *
     * @return 分类列表
     */
    List<CategoryVO> list();

    /**
     * 按作品 ID 集合批量查询其所属的启用分类（含 workId，便于按作品分组）。
     *
     * @param workIds 作品 ID 列表，不允许为空
     * @return 作品-分类关联行，按分类 sort_order 升序
     */
    List<WorkCategoryVO> selectByWorkIds(@Param("workIds") List<String> workIds);

    /**
     * 按 ID 集合查询启用中的分类 ID（status=1）。
     *
     * 发布/编辑作品时用于校验提交的 categoryIds 是否存在且可用，
     * 避免把无效分类写入 work_categories 关联表。
     *
     * @param ids 待校验的分类 ID 列表
     * @return 命中的分类 ID（可能少于入参，缺失即为不存在的分类）
     */
    List<Long> selectEnabledIds(@Param("ids") List<Long> ids);
}
