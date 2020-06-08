package com.github.autobump.bitbucket.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Getter
public class MembersResponseDto {
    private int pagelen;
    private List<Member> values;
    private String next;

    public Map<String,String> getMembers(){
        return values.stream()
                .collect(Collectors.toMap(m -> m.user.displayName, m -> m.user.uuid, (a, b) -> b));
    }

    @Data
    @Getter
    public static class Member {
        User user;


        @Data
        private static class User{
            @JsonProperty(value="display_name")
            String displayName;
            String uuid;
        }
    }
}
