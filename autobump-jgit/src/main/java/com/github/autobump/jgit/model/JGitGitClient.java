package com.github.autobump.jgit.model;

import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import com.github.autobump.jgit.exception.UnsupportedTypeException;
import com.github.autobump.maven.model.MavenWorkspace;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

public class JGitGitClient implements GitClient {
    @Override
    public Workspace clone(URI uri) {
        try(Repository repo = Git.cloneRepository().setURI(uri.toString())
                .setDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString()))
                .call().getRepository()) {

            return getWorkspace(repo.getDirectory().getPath().replace(".git", ""));
        } catch (GitAPIException e) {
            throw new GitException("something went wrong while cloneing the repo", e);
        }
    }

    private Workspace getWorkspace(String path) {
        Map<String, String> typemap = Map.of("Maven", "pom.xml");
        for (String type :
                typemap.keySet()) {
            File tmpDir = new File(path + "/" + typemap.get(type));
            if (tmpDir.exists() && isMaven(type)) {
                return new MavenWorkspace(path);
            }
        }
        throw new UnsupportedTypeException("could not find dependency file");
    }

    private boolean isMaven(String type) {
        return "Maven".equals(type);
    }
}
