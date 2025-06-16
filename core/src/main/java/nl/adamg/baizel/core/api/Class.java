package nl.adamg.baizel.core.api;

import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Model:  [nl.adamg.baizel.core.model.Class]
public interface Class {
    String canonicalName();
    List<String> imports();
}
