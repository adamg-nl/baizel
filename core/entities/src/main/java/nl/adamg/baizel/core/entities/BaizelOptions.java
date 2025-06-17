package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;
import nl.adamg.baizel.internal.common.annotations.Entity;

/// - API:    [nl.adamg.baizel.core.api.BaizelOptions]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelOptions]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelOptionsImpl]
@SuppressWarnings("JavadocReference")
@Entity
public class BaizelOptions implements Serializable {
    public int workerCount;
    public String projectRoot;

    //region generated code
    public BaizelOptions(int workerCount, String projectRoot) {
        this.workerCount = workerCount;
        this.projectRoot = projectRoot;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        BaizelOptions that = (BaizelOptions) object;
        return workerCount == that.workerCount && Objects.equals(projectRoot, that.projectRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerCount, projectRoot);
    }
    //endregion
}
