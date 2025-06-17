package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.BaizelException;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.java.typeref.TypeRef2;

public class Issue {
    public static BaizelException critical(BaizelErrors error, String... details) {
        throw new BaizelException(new nl.adamg.baizel.core.entities.Issue(
                error.name(),
                error.exitCode,
                Items.map(new TypeRef2<>() {}, details),
                error.message
        ));
    }
}
