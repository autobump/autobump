---
layout: page
title: About
permalink: /about/
---

# What is Autobump?

Autobump is a bot that automatically creates pull-requests to keep dependencies of maven projects up to date. It scans the pom.xml files, and automatically detects out-of-date dependencies using Maven central. It then creates a pull-request for every outdated dependency.


# What problem does Autobump solve?

It is important to regularly keep the dependencies of your projects up to date. External libraries are habitually updated to solve security issues and other vulnerabilities. However, it is time consuming and not very exciting to be looking for updates, implementing them in the build files and testing whether everything still runs as it should. When developers wait too long with these necessary updates, they not only fail to take advantage of improved features, but also risk to break things once they finally do make the necessary updates. 
Autobump takes this arduous task off the plate of developers, and incrementally updates all dependencies when new versions are released. It relies on the existing build pipelines of the repository to check whether anything breaks when the update is implemented.


[jekyll-organization]: https://github.com/jekyll
