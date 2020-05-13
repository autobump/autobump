package com.github.autobump.aws.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.github.autobump.aws.model.AutoBumpRequest;
import com.github.autobump.aws.model.AutoBumpResponse;
import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketUrlHelper;
import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenIgnoreRepository;
import com.github.autobump.maven.model.MavenVersionRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class AutoBumpHandler implements RequestStreamHandler {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        List<String> errors = new ArrayList<>();
        AutoBumpResponse response = getAutoBumpResponse(input, errors);
        try(PrintWriter writer =
                new PrintWriter(new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.US_ASCII)))){
            writer.write(gson.toJson(response));
        }
    }

    @SuppressWarnings("PMD")
    private AutoBumpResponse getAutoBumpResponse(InputStream input, List<String> errors)
            throws IOException {
        AutoBumpResponse response = new AutoBumpResponse(null, errors);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.US_ASCII))) {
            AutoBumpRequest autoBumpRequest = gson.fromJson(reader, AutoBumpRequest.class);
            errors.addAll(checkRequest(autoBumpRequest));
            if (errors.isEmpty()) {
                var result = doAutoBump(autoBumpRequest);
                response.setNumberOfBumps(result.getNumberOfBumps());
            }
        } catch (RuntimeException exception) {
            errors.add(exception.getMessage());
        }
        return response;
    }

    private AutobumpResult doAutoBump(AutoBumpRequest autoBumpRequest) {
        var versionrepo = new MavenVersionRepository(autoBumpRequest.getRepoUrl() == null ?
                "https://repo1.maven.org/maven2" : autoBumpRequest.getRepoUrl());
        var provider = new BitBucketGitProvider(new BitBucketAccount(autoBumpRequest.getUsername(),
                autoBumpRequest.getPassword()),
                autoBumpRequest.getApiUrl() == null ? "https://api.bitbucket.org/2.0" : autoBumpRequest.getApiUrl());
        var ignorerepo = new MavenIgnoreRepository(autoBumpRequest.getIgnoreMap() == null ?
                Collections.emptyMap() : autoBumpRequest.getIgnoreMap());
        var usecase = AutobumpUseCase.builder()
                .versionRepository(versionrepo)
                .urlHelper(new BitBucketUrlHelper())
                .dependencyBumper(new MavenDependencyBumper())
                .dependencyResolver(new MavenDependencyResolver())
                .gitClient(new JGitGitClient(autoBumpRequest.getUsername(), autoBumpRequest.getPassword()))
                .gitProvider(provider)
                .uri(URI.create(autoBumpRequest.getGitUrl()))
                .ignoreRepository(ignorerepo)
                .build();
        return usecase.doAutoBump();
    }

    private List<String> checkRequest(AutoBumpRequest autoBumpRequest) {
        List<String> errors = new ArrayList<>();
        if (autoBumpRequest.getUsername() == null) {
            errors.add("no username provided");
        }
        if (autoBumpRequest.getPassword() == null) {
            errors.add("no password provided");
        }
        errors.addAll(checkGitUrl(autoBumpRequest.getGitUrl()));
        return errors;
    }

    private List<String> checkGitUrl(String gitUrl) {
        List<String> errors = new ArrayList<>();
        if (gitUrl != null) {
            var matcher = Pattern
                    .compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]" +
                            "{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)").matcher(gitUrl);
            if (!matcher.matches()) {
                errors.add(gitUrl + " was not recognized as a url");
            }
        } else {
            errors.add("no gitUrl provided");
        }
        return errors;
    }
}
