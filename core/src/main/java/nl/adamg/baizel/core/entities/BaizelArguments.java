package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.BaizelArguments]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelArguments]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelArgumentsImpl]
public class BaizelArguments implements Serializable {
    public BaizelOptions options;
    public Invocation invocation;

    //region generated code
    public BaizelArguments(
            BaizelOptions options,
            Invocation invocation
    ) {
        this.options = options;
        this.invocation = invocation;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        BaizelArguments that = (BaizelArguments) object;
        return Objects.equals(options, that.options) && Objects.equals(invocation, that.invocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options, invocation);
    }
    //endregion
}
