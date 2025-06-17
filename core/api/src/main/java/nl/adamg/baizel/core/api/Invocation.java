package nl.adamg.baizel.core.api;

import java.util.List;
import java.util.Set;

/// - API:    [nl.adamg.baizel.core.api.Invocation]
/// - Entity: [nl.adamg.baizel.core.entities.Invocation]
/// - Impl:   [nl.adamg.baizel.core.impl.InvocationImpl]
@SuppressWarnings("JavadocReference")
public interface Invocation {
    Set<String> tasks();
    List<String> taskArgs();
    Set<Target> targets();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
