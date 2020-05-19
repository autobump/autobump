package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

@Builder
public class PushUseCase {
    GitClient gitClient;

    public void doPush(Workspace workspace, Bump bump, String branchName){
        gitClient.commitToExistingBranch(workspace, bump, branchName);
    }

}
