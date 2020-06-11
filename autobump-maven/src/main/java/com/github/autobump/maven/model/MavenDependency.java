package com.github.autobump.maven.model;

import com.github.autobump.core.model.domain.Dependency;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.apache.maven.model.InputLocation;

@EqualsAndHashCode(callSuper = true, exclude = "inputLocation")
@ToString(callSuper = true)
@Value
@SuperBuilder
public class MavenDependency extends Dependency {
    InputLocation inputLocation;
    DependencyType type;

    public Dependency getAsDependency() {
        return Dependency.builder()
                .group(this.getGroup())
                .version(this.getVersion())
                .name(this.getName())
                .build();
    }
}
