package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/// - API:    [nl.adamg.baizel.core.api.BaizelArguments]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelArguments]
/// - Model: [nl.adamg.baizel.core.impl.BaizelArgumentsImpl]
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
    //endregion
}
