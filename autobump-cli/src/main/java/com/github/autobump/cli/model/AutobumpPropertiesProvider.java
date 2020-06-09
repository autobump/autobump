package com.github.autobump.cli.model;

import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.util.Map;

@Getter
@Command(mixinStandardHelpOptions = true, name = "autobump",
        description = "%nAutobump automatically creates a pull-request for every outdated " +
                "dependency in a Bitbucket repository.%n",
        version = {"Version: @|yellow Autobump 1.0|@"},
        footer = "Copyright(c) 2020 Xplore - distributed under the terms of the MIT license" +
                "%nvisit https://github.com/autobump/autobump for more information")
public final class AutobumpPropertiesProvider {
    private static AutobumpPropertiesProvider instance;
    @Option(names = {"-u", "--username"}, description = "User name for your remote repository", required = true)
    private String username;
    @Option(names = {"-l", "--url"}, paramLabel = "REPOURL", description = "project repository url", required = true)
    private URI url;
    @Option(names = {"-p", "--password"}, description = "Password for your remote repository", required = true)
    private String password;
    @Option(names = {"-r", "--repourl"}, description = "Public repository url for dependency version information",
            defaultValue = "https://repo1.maven.org/maven2")
    private String repositoryUrl;
    @Option(names = {"-b", "--bbapiurl"}, description = "Bitbucket Api url",
            defaultValue = "https://api.bitbucket.org/2.0")
    private String bbApiUrl;
    @Option(names = {"-g", "--ghapiurl"}, description = "Github Api url",
            defaultValue = "https://api.github.com")
    private String ghApiUrl;
    @Option(names = {"-i", "--ignored"}, description = "Dependencies to ignore for updates including update" +
            " type separated by comma", split = ",")
    private Map<String, String> ignoreDependencies;

    private AutobumpPropertiesProvider() {
    }

    public static AutobumpPropertiesProvider getInstance() {
        synchronized (AutobumpPropertiesProvider.class) {
            if (instance == null) {
                instance = new AutobumpPropertiesProvider();
            }
            return instance;
        }
    }
}
