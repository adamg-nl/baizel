package nl.adamg.baizel.core.api;

/// - API:    [nl.adamg.baizel.core.api.BaizelArguments]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelArguments]
/// - Model:  [nl.adamg.baizel.core.impl.BaizelArgumentsImpl]
public interface BaizelArguments {
    BaizelOptions options();
    Invocation invocation();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
