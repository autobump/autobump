package model;

import lombok.Data;

@Data
public class PullRequestBody {
    private final String title;
    private final PullRequestBody.Source source;

    @Data
    static class Source{
        private final PullRequestBody.Branch branch;
    }

    @Data
    static class Branch{
        private final String name;
    }
}
