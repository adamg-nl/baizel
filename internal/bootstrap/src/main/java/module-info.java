module nl.adamg.baizel.internal.bootstrap {
    exports nl.adamg.baizel.internal.bootstrap;

    requires org.apache.maven.maven.resolver.provider;
    requires org.apache.maven.maven.model.builder;
    requires org.apache.maven.maven.builder.support;
    requires org.apache.maven.maven.artifact;
    requires org.apache.maven.maven.model;
    requires org.apache.maven.resolver.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.maven.resolver.transport.file;
    requires org.apache.maven.resolver.maven.resolver.transport.http;
    requires org.apache.maven.resolver.maven.resolver.api;
    requires org.apache.maven.resolver.maven.resolver.impl;
    requires org.apache.maven.resolver.maven.resolver.named.locks;
    requires org.apache.maven.resolver.maven.resolver.spi;
    requires org.apache.maven.resolver.maven.resolver.util;
    requires org.codehaus.plexus.plexus.interpolation;
    requires org.codehaus.plexus.plexus.utils;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.slf4j.jcl.over.slf4j;
    requires org.slf4j.slf4j.simple;
    requires org.slf4j.slf4j.api;
}
