package model;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import exception.BranchNotFoundException;
import exception.RemoteNotFoundException;
import exception.UnauthorizedException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Setter
@Getter
public class BitBucketGitProvider implements GitProvider {
    private static final String API_LINK = "https://api.bitbucket.org/2.0";
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private BitBucketAccount user;

    public BitBucketGitProvider(BitBucketAccount user) {
        this.user = user;
    }

    @Override
    public void MakePullRequest(PullRequest pullRequest) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(getBody(pullRequest.getTitle(),
                            pullRequest.getBranchName())))
                    .uri(URI.create(String.format("%s/repositories/%s/%s/pullrequests",
                            API_LINK,
                            pullRequest.getRepoOwner(),
                            pullRequest.getProjectName())))
                    .header("Authorization",
                            "Basic " + encodeUsernamePassword(user.getUsername(), user.getPassword()))
                    .header("Content-Type",
                            "application/json")
                    .build();

            executeRequest(request);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String getBody(String title, String branchName) {
        return String.format("{\"title\": \"%s\",\"source\": {\"branch\": {\"name\": \"%s\"}}}",
                title,
                branchName);
    }

    private void executeRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new RemoteNotFoundException("Could not find remote repository");
        } else if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new UnauthorizedException("the user is not authorized to make a pull request on this repository");
        } else if (response.statusCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new BranchNotFoundException("could not find branch");
        }
    }

    private String encodeUsernamePassword(String username, String password) {
        return Base64
                .getEncoder()
                .encodeToString(String.format("%s:%s", username, password)
                        .getBytes(StandardCharsets.UTF_8));
    }
}
