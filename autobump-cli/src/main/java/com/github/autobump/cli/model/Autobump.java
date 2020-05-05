package com.github.autobump.cli.model;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Command(
        name = "autobump",
        description = "automatically creates a pull-request for every outdated dependency in a project"
)
public class Autobump implements Callable<Integer> {
    @CommandLine.Option(
            names = {
                    "--url"
            },
            paramLabel = "REPOURL",
            description = "project repository url"
    )
    private String url;
    public static void main(String[] args) {
        int exitcode = new CommandLine(new Autobump()).execute(args);
        if (exitcode != 0) {
            throw new RuntimeException(Integer.toString(exitcode));
        }
    }

    @Override
    public Integer call() throws Exception {
        Logger.getAnonymousLogger().info(url);
        return 0;
    }
}
