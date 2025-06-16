module nl.adamg.baizel.internal.maven {
    exports nl.adamg.baizel.internal.maven;
    requires nl.adamg.baizel.internal.common.annotations;

    requires maven.artifact;
    requires maven.builder.support;
    requires maven.model.builder;
    requires maven.model;
    requires maven.resolver.provider;
    requires org.apache.commons.logging;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.impl;
    requires org.apache.maven.resolver.named;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.transport.file;
    requires org.apache.maven.resolver.transport.http;
    requires org.apache.maven.resolver.util;
    requires org.apache.maven.resolver;
    requires org.slf4j.simple;
    requires org.slf4j;
    requires plexus.interpolation;
    requires plexus.utils;
}
