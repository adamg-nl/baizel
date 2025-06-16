project com.foobar {
    repository https://repo1.maven.org/maven2/;

    dependencies {
        com.google.code.findbugs:jsr305:3.0.2                              { jsr305; }
        org.codehaus.plexus:plexus-interpolation:1.27                      { org.codehaus.plexus.plexus; }
        org.slf4j:slf4j-api:1.7.36                                         { org.slf4j.slf4j.api; }
        net.java.truecommons:truecommons-shed:2.5.0                        { net.java.truecommons.shed; }
        org.xerial:sqlite-jdbc:3.49.1.0                                    { org.xerial.sqlitejdbc; }
        com.fasterxml.jackson.core:jackson-core:2.17.0                     { com.fasterxml.jackson.core; }
    }
}
