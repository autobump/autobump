import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BitBucketGitProvider {
    public void MakePullRequest()  {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("https://api.bitbucket.org/2.0/repositories/grietvermeesch/testmavenproject/pullrequests");


            String encoding = Base64.getEncoder().encodeToString(("glenn.schrooyen@student.kdg.be" + ":" + "AutoBump2209").getBytes(StandardCharsets.UTF_8));
            httppost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
// Request parameters and other properties.
            String data = "{\"title\": \"heyhey\",\"source\": {\"branch\": {\"name\": \"test\"}}}";
            StringEntity stringEntity = new StringEntity(data);
            httppost.setEntity(stringEntity);
            httppost.setHeader("Content-Type", "application/json");

//Execute and get the response.
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    // do something useful
                    System.out.println(new String(instream.readAllBytes()));
                }
            }
        } catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }
}
