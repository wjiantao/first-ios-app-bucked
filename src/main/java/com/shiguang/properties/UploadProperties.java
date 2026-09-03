package com.shiguang.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置（application.yml 中 shiguang.upload 前缀）。
 */
@Component
@ConfigurationProperties(prefix = "shiguang.upload")
@Data
public class UploadProperties {

    /** 上传文件保存目录，支持相对路径（相对进程工作目录）或绝对路径 */
    private String dir;
}
