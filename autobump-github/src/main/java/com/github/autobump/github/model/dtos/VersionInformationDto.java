package com.github.autobump.github.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VersionInformationDto {
    @JsonProperty(value = "html_url")
    String htmlUrl;
    @JsonProperty(value = "tag_name")
    String tagName;
    String body;
}
