project nl.adamg.baizel {
    repository https://repo1.maven.org/maven2/;

    dependencies {
        com.google.code.findbugs:jsr305:3.0.2                           { jsr305; }
        org.apache.httpcomponents:httpclient:4.5.14                     { org.apache.httpcomponents.httpclient; }
        org.apache.httpcomponents:httpcore:4.4.16                       { org.apache.httpcomponents.httpcore; }
        org.apache.maven.resolver:maven-resolver-api:1.9.20             { org.eclipse.aether; }
        org.apache.maven.resolver:maven-resolver-connector-basic:1.9.20 { org.eclipse.aether.connector.basic; }
        org.apache.maven.resolver:maven-resolver-impl:1.9.20            { org.eclipse.aether.impl; org.eclipse.aether.internal.impl; }
        org.apache.maven.resolver:maven-resolver-named-locks:1.9.20     { org.apache.maven.resolver.maven.resolver.named.locks; }
        org.apache.maven.resolver:maven-resolver-spi:1.9.20             { org.eclipse.aether.spi; }
        org.apache.maven.resolver:maven-resolver-transport-file:1.9.20  { org.apache.maven.resolver.maven.resolver.transport.file; }
        org.apache.maven.resolver:maven-resolver-transport-http:1.9.20  { org.apache.maven.resolver.maven.resolver.transport.http; }
        org.apache.maven.resolver:maven-resolver-util:1.9.20            { org.apache.maven.resolver.maven.resolver.util; }
        org.apache.maven:maven-artifact:3.9.8                           { org.apache.maven.maven.artifact; }
        org.apache.maven:maven-builder-support:3.9.8                    { org.apache.maven.maven.builder.support; }
        org.apache.maven:maven-model-builder:3.9.8                      { org.apache.maven.maven.model.builder; }
        org.apache.maven:maven-model:3.9.8                              { org.apache.maven.maven.model; }
        org.apache.maven:maven-resolver-provider:3.9.8                  { org.apache.maven.repository.internal; }
        org.codehaus.plexus:plexus-interpolation:1.27                   { org.codehaus.plexus.plexus; }
        org.codehaus.plexus:plexus-utils:3.5.1                          { org.codehaus.plexus.plexus.utils; }
        org.slf4j:jcl-over-slf4j:1.7.36                                 { org.slf4j.jcl.over.slf4j; }
        org.slf4j:slf4j-api:1.7.36                                      { org.slf4j.slf4j.api; }
        org.slf4j:slf4j-simple:1.7.36                                   { org.slf4j.slf4j.simple; }
        org.xerial:sqlite-jdbc:3.49.1.0                                 { org.xerial.sqlitejdbc; }
    }
}
