package cn.iocoder.yudao.module.dorm.service;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormTravelVO.PositionReq;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

final class DormImage {
    private DormImage() {}
    static DormTravelService.PhotoPayload read(MultipartFile file) {
        require(file != null && !file.isEmpty() && file.getSize() <= 5 * 1024 * 1024, "照片必须为1字节至5MB");
        ImageReader reader = null;
        try {
            byte[] bytes = file.getBytes();
            require(bytes.length > 0 && bytes.length <= 5 * 1024 * 1024, "照片超过5MB或为空");
            try (MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                require(readers.hasNext(), "照片内容无法识别");
                reader = readers.next(); reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                require("jpeg".equals(format) || "png".equals(format), "照片仅支持实际JPEG或PNG格式");
                require((long) reader.getWidth(0) * reader.getHeight(0) <= 20000000, "照片像素总量不能超过2000万");
                require(reader.read(0) != null, "照片内容无法解码");
                DormTravelService.PhotoPayload result = new DormTravelService.PhotoPayload();
                result.setContent(bytes); result.setContentType("png".equals(format) ? "image/png" : "image/jpeg");
                return result;
            }
        } catch (IOException | RuntimeException error) {
            if (error instanceof cn.iocoder.yudao.framework.common.exception.ServiceException)
                throw (cn.iocoder.yudao.framework.common.exception.ServiceException) error;
            require(false, "照片内容损坏或无法读取"); return null;
        } finally { if (reader != null) reader.dispose(); }
    }
    static String fingerprint(PositionReq req, byte[] image) {
        String position = req.getLatitude().stripTrailingZeros().toPlainString() + "|"
                + req.getLongitude().stripTrailingZeros().toPlainString() + "|"
                + req.getAccuracy().stripTrailingZeros().toPlainString() + "|" + req.getCapturedAt();
        return digest((position + "|" + (image == null ? "" : digest(image))).getBytes(StandardCharsets.UTF_8));
    }
    private static String digest(byte[] content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format(Locale.ROOT, "%02x", value & 255));
            return result.toString();
        } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
}
