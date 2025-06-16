package nl.adamg.baizel.core.api;

import java.nio.file.Path;

/// - API:    [nl.adamg.baizel.core.api.BaizelOptions]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelOptions]
/// - Model:  [nl.adamg.baizel.core.model.BaizelOptions]
public interface BaizelOptions {
    int workerCount();
    Path projectRoot();
}
