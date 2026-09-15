package com.smartInvoice.admin_service.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blog")
public class BlogProperties {
	private int maxPageSize = 50;
	private int maxTagsPerPost = 10;
	private int maxProductLinksPerPost = 8;
	private boolean coverImageRequiredForPublish = false;
	private long maxUploadBytes = 5 * 1024 * 1024;
	private String mediaBaseUrl = "";
	private String mediaUploadDir = "uploads/blog";
	private String nextjsRevalidationUrl = "";
	private String nextjsRevalidationSecret = "";

	public int getMaxPageSize() { return maxPageSize; }
	public void setMaxPageSize(int maxPageSize) { this.maxPageSize = maxPageSize; }
	public int getMaxTagsPerPost() { return maxTagsPerPost; }
	public void setMaxTagsPerPost(int maxTagsPerPost) { this.maxTagsPerPost = maxTagsPerPost; }
	public int getMaxProductLinksPerPost() { return maxProductLinksPerPost; }
	public void setMaxProductLinksPerPost(int maxProductLinksPerPost) { this.maxProductLinksPerPost = maxProductLinksPerPost; }
	public boolean isCoverImageRequiredForPublish() { return coverImageRequiredForPublish; }
	public void setCoverImageRequiredForPublish(boolean coverImageRequiredForPublish) { this.coverImageRequiredForPublish = coverImageRequiredForPublish; }
	public long getMaxUploadBytes() { return maxUploadBytes; }
	public void setMaxUploadBytes(long maxUploadBytes) { this.maxUploadBytes = maxUploadBytes; }
	public String getMediaBaseUrl() { return mediaBaseUrl; }
	public void setMediaBaseUrl(String mediaBaseUrl) { this.mediaBaseUrl = mediaBaseUrl; }
	public String getMediaUploadDir() { return mediaUploadDir; }
	public void setMediaUploadDir(String mediaUploadDir) { this.mediaUploadDir = mediaUploadDir; }
	public String getNextjsRevalidationUrl() { return nextjsRevalidationUrl; }
	public void setNextjsRevalidationUrl(String nextjsRevalidationUrl) { this.nextjsRevalidationUrl = nextjsRevalidationUrl; }
	public String getNextjsRevalidationSecret() { return nextjsRevalidationSecret; }
	public void setNextjsRevalidationSecret(String nextjsRevalidationSecret) { this.nextjsRevalidationSecret = nextjsRevalidationSecret; }
}
