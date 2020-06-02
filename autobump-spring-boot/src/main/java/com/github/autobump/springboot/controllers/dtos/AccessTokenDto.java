package com.github.autobump.springboot.controllers.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccessTokenDto {
    @JsonProperty("access_token")
    private String token;
}
