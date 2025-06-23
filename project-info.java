project nl.adamg.baizel {
    group nl.adamg;
    www https://adamg.nl/baizel;
    git https://github.com/adamg-nl/baizel.git;
    license Copyright (C) 2025 AdamG.NL\u003B All rights reserved.;
    repository maven https://repo1.maven.org/maven2/;

    custom metadata {
        example 42;
        foo {
            bar;
        }
    }

    dependencies {
        com.google.code.findbugs:jsr305:3.0.2                               { javax.annotation; jsr305; }
        org.apache.httpcomponents:httpclient:4.5.14                         { org.apache.httpcomponents.httpclient; }
        org.apache.httpcomponents:httpcore:4.4.16                           { org.apache.httpcomponents.httpcore; }
        org.apache.maven.resolver:maven-resolver-api:1.9.20                 { org.eclipse.aether; org.apache.maven.resolver; }
        org.apache.maven.resolver:maven-resolver-connector-basic:1.9.20     { org.eclipse.aether.connector.basic; org.apache.maven.resolver.connector.basic; }
        org.apache.maven.resolver:maven-resolver-impl:1.9.20                { org.eclipse.aether.impl; org.eclipse.aether.internal.impl; org.apache.maven.resolver.impl; }
        org.apache.maven.resolver:maven-resolver-named-locks:1.9.20         { org.eclipse.aether.named; org.apache.maven.resolver.named; }
        org.apache.maven.resolver:maven-resolver-spi:1.9.20                 { org.eclipse.aether.spi; org.apache.maven.resolver.spi; }
        org.apache.maven.resolver:maven-resolver-transport-file:1.9.20      { org.eclipse.aether.transport.file; org.apache.maven.resolver.transport.file; }
        org.apache.maven.resolver:maven-resolver-transport-http:1.9.20      { org.eclipse.aether.transport.http; org.apache.maven.resolver.transport.http; }
        org.apache.maven.resolver:maven-resolver-util:1.9.20                { org.eclipse.aether.util; org.apache.maven.resolver.util; }
        org.apache.maven:maven-artifact:3.9.8                               { org.apache.maven.artifact; maven.artifact; }
        org.apache.maven:maven-builder-support:3.9.8                        { org.apache.maven.building; maven.builder.support; }
        org.apache.maven:maven-model-builder:3.9.8                          { org.apache.maven.model.building; maven.model.builder; }
        org.apache.maven:maven-model:3.9.8                                  { org.apache.maven.model; maven.model; }
        org.apache.maven:maven-resolver-provider:3.9.8                      { org.apache.maven.repository.internal; maven.resolver.provider; }
        org.codehaus.plexus:plexus-interpolation:1.27                       { org.codehaus.plexus.interpolation; plexus.interpolation; }
        org.codehaus.plexus:plexus-utils:3.5.1                              { org.codehaus.plexus.util; plexus.utils; }
        org.slf4j:jcl-over-slf4j:1.7.36                                     { org.apache.commons.logging; }
        org.slf4j:slf4j-api:1.7.36                                          { org.slf4j; }
        org.slf4j:slf4j-simple:1.7.36                                       { org.slf4j.simple; }
        net.java.truevfs:truevfs-access:0.14.0                              { net.java.truevfs.access; }
        net.java.truevfs:truevfs-comp-zipdriver:0.14.0                      { net.java.truevfs.comp.zipdriver; }
        net.java.truevfs:truevfs-driver-zip:0.14.0                          { net.java.truevfs.driver.zip }
        net.java.truevfs:truevfs-kernel-spec:0.14.0                         { net.java.truevfs.kernel.spec; }
        net.java.truecommons:truecommons-shed:2.5.0                         { net.java.truecommons.shed; truecommons.shed; }
        net.java.truecommons:truecommons-key-default:2.5.0                  { net.java.truecommons.key.def; }
        org.xerial:sqlite-jdbc:3.49.1.0                                     { org.xerial.sqlitejdbc; }
        com.fasterxml.jackson.core:jackson-core:2.17.0                      { com.fasterxml.jackson.core; }
        com.fasterxml.jackson.core:jackson-annotations:2.17.0               { com.fasterxml.jackson.annotation; }
        com.fasterxml.jackson.core:jackson-databind:2.17.0                  { com.fasterxml.jackson.databind; }
        com.fasterxml.jackson.module:jackson-module-parameter-names:2.17.0  { com.fasterxml.jackson.module.paramnames; }
    }
}
