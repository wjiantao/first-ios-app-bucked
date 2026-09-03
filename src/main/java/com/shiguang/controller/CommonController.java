package com.shiguang.controller;

import com.shiguang.context.UserContext;
import com.shiguang.properties.UploadProperties;
import com.shiguang.result.Result;
import com.shiguang.service.AuthService;
import com.shiguang.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/common")
@Tag(name = "通用")
public class CommonController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /** 单文件大小上限：5MB */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** 只允许上传图片，扩展名白名单 */
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    /** 上传文件对外可访问的 URL 前缀，与 WebMvcConfiguration 中的静态资源映射一致 */
    private static final String UPLOAD_URL_PREFIX = "/uploads/";

    @Autowired
    private UploadProperties uploadProperties;

    /**
     * 图片上传。
     *
     * 文件名使用 UUID 重新生成，不信任客户端原始文件名，避免路径穿越与覆盖；
     * 保存目录来自配置（shiguang.upload.dir），不再写死在代码里。
     *
     * @param file 上传的图片文件
     * @return 可访问的图片路径，例如 /uploads/3f9c...a1b2.png
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "上传图片", description = "表单字段 file 上传图片，仅支持 jpg/jpeg/png/gif/webp，单文件不超过 5MB；"
            + "成功返回可访问的图片路径（/uploads/xxx）。")
    public Result<String> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过 5MB");
        }

        log.info("文件上传: {}", file);
        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.error("仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path uploadDir = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();

        log.info("uploadDir: {}", uploadDir);
        log.info("fileName: {}", fileName);
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(fileName));
            log.info("文件上传成功：{}{} -> {}", UPLOAD_URL_PREFIX, fileName, uploadDir.resolve(fileName));
        } catch (IOException ex) {
            log.error("文件保存失败: {}", fileName, ex);
            return Result.error(500, "文件上传失败，请稍后重试");
        }
        log.info("最后的头像返回路径: {}", UPLOAD_URL_PREFIX + fileName);

//        userService.updateAvatar(UserContext.getCurrentId(), UPLOAD_URL_PREFIX + fileName);
        return Result.success(UPLOAD_URL_PREFIX + fileName);
    }

    /** 从原始文件名中提取小写扩展名；没有扩展名时返回 null。 */
    private String resolveExtension(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            return null;
        }
        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

}
