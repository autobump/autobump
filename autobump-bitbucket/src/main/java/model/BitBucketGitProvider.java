package model;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import exception.BranchNotFoundException;
import exception.RemoteNotFoundException;
import exception.UnauthorizedException;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Setter
@Getter
public class BitBucketGitProvider implements GitProvider {
    private static final String API_LINK = "https://api.bitbucket.org/2.0";
    private final HttpClient httpClient = HttpClients.createDefault();
    private BitBucketAccount user;

    public BitBucketGitProvider(BitBucketAccount user) {
        this.user = user;
    }

    @Override
    public void MakePullRequest(PullRequest pullRequest) {
        try {
            HttpPost httppost = new HttpPost(
                    String.format("%s/repositories/%s/%s/pullrequests",
                            API_LINK,
                            pullRequest.getRepoOwner(),
                            pullRequest.getProjectName()));
            addHeaders(httppost);
            addBody(pullRequest.getTitle(), pullRequest.getBranchName(), httppost);
            executeRequest(httppost);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addBody(String title, String branchName, HttpPost httppost) throws UnsupportedEncodingException {
        String data = String.format("{\"title\": \"%s\",\"source\": {\"branch\": {\"name\": \"%s\"}}}",
                title,
                branchName);
        httppost.setEntity(new StringEntity(data));
    }

    private void addHeaders(HttpPost httppost) {
        if (user != null) {
            httppost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodeUsernamePassword(user.getUsername(), user.getPassword()));
        }
        httppost.setHeader("Content-Type", "application/json");
    }

    private void executeRequest(HttpPost httppost) throws IOException {
        HttpResponse response = httpClient.execute(httppost);
        if (response.getStatusLine().getStatusCode() == 404) {
            throw new RemoteNotFoundException("Could not find remote repository");
        } else if (response.getStatusLine().getStatusCode() == 401) {
            throw new UnauthorizedException("the user is not authorized to make a pull request on this repository");
        } else if (response.getStatusLine().getStatusCode() == 400) {
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
