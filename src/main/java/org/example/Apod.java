package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Apod {
    private final String copyright;
    private final String explanation;
    private final String hdurl;
    private final String date;
    private final String mediaType;
    private final String serviceVersion;
    private final String title;
    private final String url;
    private final String thumbnailUrl;

    public Apod(
            @JsonProperty("copyright") String copyright,
            @JsonProperty("explanation") String explanation,
            @JsonProperty("hdurl") String hdurl,
            @JsonProperty("date") String date,
            @JsonProperty("media_type") String mediaType,
            @JsonProperty("service_version") String serviceVersion,
            @JsonProperty("title") String title,
            @JsonProperty("url") String url,
            @JsonProperty("thumbnail_url") String thumbnailUrl
    ) {
        this.copyright = copyright;
        this.explanation = explanation;
        this.hdurl = hdurl;
        this.date = date;
        this.mediaType = mediaType;
        this.serviceVersion = serviceVersion;
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getHdurl() {
        return hdurl;
    }

    public String getUrl() {
        return url;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    @Override
    public String toString() {
        return "APOD for " + date +
                //"\n date = " + date +
                "\n title = " + title +
                "\n explanation = " + explanation +
                "\n copyright = " + copyright +
                "\n mediaType = " + mediaType +
                "\n url = " + url +
                "\n hdurl = " + hdurl +
                "\n service_version = " + serviceVersion +
                "\n thumbnail_url = " + thumbnailUrl;
    }

}