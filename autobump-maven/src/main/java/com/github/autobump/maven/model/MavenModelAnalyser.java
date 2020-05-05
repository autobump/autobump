package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenModelAnalyser {
    public static final String DEPENDENCY_FILENAME = "pom.xml";
    private static final Pattern VERSION_PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+)}");

    public Model getModel(Workspace workspace) {
        try (Reader dependencyDocument = workspace.getDependencyDocument(DEPENDENCY_FILENAME)) {
            InputSource inputSource = new InputSource();
            inputSource.setLocation(workspace.getProjectRoot() + "/pom.xml");
            return new MavenXpp3ReaderEx()
                    .read(dependencyDocument, true, inputSource);
        } catch (XmlPullParserException | IOException e) {
            throw new DependencyParserException("Parser threw an error.", e);
        }
    }

    public String getVersionFromProperties(Model model, String pluginVersionData) {
        if (pluginVersionData == null) {
            return null;
        }
        String returnVersion;
        Matcher matcher = VERSION_PROPERTY_PATTERN.matcher(pluginVersionData);
        if (!matcher.matches()) {
            returnVersion = pluginVersionData;
        }else {
            returnVersion = model.getProperties().getProperty(matcher.group(1));
        }
        return returnVersion;
    }

    public String getVersionFromProperties(Model model, String pluginVersionData, Profile profile) {
        Matcher matcher = VERSION_PROPERTY_PATTERN.matcher(pluginVersionData);
        if (!matcher.matches()) {
            return pluginVersionData;
        }
        String value = profile.getProperties().getProperty(matcher.group(1));
        if (value == null){
            value = model.getProperties().getProperty(matcher.group(1));
        }
        return value;
    }
}
