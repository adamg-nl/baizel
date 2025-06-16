package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/// - API:    [nl.adamg.baizel.core.api.BaizelOptions]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelOptions]
/// - Model: [nl.adamg.baizel.core.impl.BaizelOptionsImpl]
public final class BaizelOptions implements Serializable {
    public int workerCount;
    public String projectRoot;

    //region generated code
    public BaizelOptions(int workerCount, String projectRoot) {
        this.workerCount = workerCount;
        this.projectRoot = projectRoot;
    }
    //endregion
}
