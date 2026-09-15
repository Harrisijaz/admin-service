package com.smartInvoice.admin_service.service;

import com.smartInvoice.admin_service.dto.BlogDtos.UploadResponse;
import com.smartInvoice.admin_service.domain.AdminActionType;
import com.smartInvoice.admin_service.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class BlogMediaService {
	private static final Map<String, String> MIME_TO_EXT = Map.of(
			"image/jpeg", "jpg",
			"image/png", "png",
			"image/webp", "webp",
			"image/avif", "avif"
	);
	private final BlogProperties properties;
	private final AuditService auditService;

	public BlogMediaService(BlogProperties properties, AuditService auditService) {
		this.properties = properties;
		this.auditService = auditService;
	}

	public UploadResponse upload(MultipartFile file, String altText, AdminPrincipal admin) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_EMPTY_UPLOAD", "Image file is required.");
		}
		if (file.getSize() > properties.getMaxUploadBytes()) {
			throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "BLOG_UPLOAD_TOO_LARGE", "Uploaded file is too large.");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		String extension = MIME_TO_EXT.get(contentType);
		if (extension == null || !filenameExtensionAllowed(file.getOriginalFilename(), extension)) {
			throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "BLOG_INVALID_MEDIA_TYPE", "Only JPEG, PNG, WEBP, and AVIF images are supported.");
		}
		ImageInfo imageInfo = inspect(file, contentType);
		try {
			Path dir = Paths.get(properties.getMediaUploadDir()).toAbsolutePath().normalize();
			Files.createDirectories(dir);
			String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
			Path target = dir.resolve(filename).normalize();
			if (!target.startsWith(dir)) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "BLOG_INVALID_FILENAME", "Invalid upload filename.");
			}
			file.transferTo(target);
			String baseUrl = properties.getMediaBaseUrl() == null ? "" : properties.getMediaBaseUrl().replaceAll("/+$", "");
			String url = baseUrl.isBlank() ? "/uploads/blog/" + filename : baseUrl + "/" + filename;
			auditService.log(admin, AdminActionType.BLOG_MEDIA_UPLOADED, filename, "Blog media uploaded.");
			return new UploadResponse(url, altText, imageInfo.width(), imageInfo.height(), contentType, file.getSize());
		} catch (ApiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "BLOG_UPLOAD_FAILED", "Could not store uploaded image.");
		}
	}

	private ImageInfo inspect(MultipartFile file, String contentType) {
		if ("image/avif".equals(contentType)) return new ImageInfo(null, null);
		try (InputStream in = file.getInputStream()) {
			BufferedImage image = ImageIO.read(in);
			if (image == null) {
				throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "BLOG_INVALID_IMAGE", "Uploaded file is not a valid image.");
			}
			return new ImageInfo(image.getWidth(), image.getHeight());
		} catch (ApiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "BLOG_INVALID_IMAGE", "Uploaded file is not a valid image.");
		}
	}

	private boolean filenameExtensionAllowed(String filename, String expected) {
		if (filename == null || !filename.contains(".")) return true;
		String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		return expected.equals(ext) || ("jpg".equals(expected) && "jpeg".equals(ext));
	}

	private record ImageInfo(Integer width, Integer height) {
	}
}
