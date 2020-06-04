package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesUrlHelper;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GithubUrlHelper extends ReleaseNotesUrlHelper {
}
