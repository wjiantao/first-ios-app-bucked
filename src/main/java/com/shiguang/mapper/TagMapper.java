package com.shiguang.mapper;

import com.shiguang.vo.TagVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签表数据访问接口。
 *
 * 与 UserMapper 一致：SQL 收敛在 resources/mapper/TagMapper.xml 中。
 * 标签为自由文本、独立于固定分类；写入时按 name 唯一键去重（INSERT IGNORE）。
 */
public interface TagMapper {

    /**
     * 批量插入标签名（幂等：已存在的同名标签被忽略，不会产生重复行）。
     *
     * @param names 已规范化、去重后的标签名列表，不允许为空
     */
    void insertIgnore(@Param("names") List<String> names);

    /**
     * 按标签名集合查询已存在的标签（用于把 name 映射为 id）。
     *
     * @param names 标签名列表，不允许为空
     * @return 命中的标签（id + name），可能与入参顺序不同
     */
    List<TagVO> selectByNameIn(@Param("names") List<String> names);

    /**
     * 按作品 ID 集合批量查询其所属标签（含 workId，便于按作品分组）。
     *
     * @param workIds 作品 ID 列表，不允许为空
     * @return 作品-标签行，按标签名升序（该排序用于页面展示）
     */
    List<TagVO> selectByWorkIds(@Param("workIds") List<String> workIds);

    /**
     * 查询库中已存在的全部标签名（去重，按名称升序）。
     *
     * 供“发布页话题联想”使用：把数据库已收录的标签作为候选，
     * 用户可直接点选，也可自行新建。
     *
     * @return 已存在的标签名列表
     */
    List<String> listNames();
}
