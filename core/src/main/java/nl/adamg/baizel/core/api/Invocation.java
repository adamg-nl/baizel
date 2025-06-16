package nl.adamg.baizel.core.api;

import java.util.List;
import java.util.Set;

/// - API:    [nl.adamg.baizel.core.api.Invocation]
/// - Entity: [nl.adamg.baizel.core.entities.Invocation]
/// - Model:  [nl.adamg.baizel.core.model.Invocation]
public interface Invocation extends Comparable<Invocation> {
    Set<String> tasks();
    List<String> taskArgs();
    Set<Target> targets();
}
