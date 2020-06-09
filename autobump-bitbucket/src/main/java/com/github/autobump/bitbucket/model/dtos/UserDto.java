package com.github.autobump.bitbucket.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDto {
    @JsonProperty(value="display_name")
    String displayName;
    String uuid;
}
