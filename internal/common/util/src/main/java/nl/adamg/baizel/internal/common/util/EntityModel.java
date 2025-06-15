package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import java.util.List;
import java.util.function.Function;

/// Provides canonical implementation of methods relevant for value-like / primitive types:
/// equals, hashCode, compareTo, auto-implemented based on given list of fields.
/// Useful for model types (business logic classes) that wrap entity types (pure data structures).
/// @param <TEntity> entity being wrapped and modelled.
public abstract class EntityModel<TEntity, TModel extends EntityModel<TEntity, TModel>> implements Comparable<TModel> {
    protected final TEntity entity;

    protected EntityModel() {
        @SuppressWarnings("unchecked")
        var cast = (TEntity) this;
        this.entity = cast;
    }

    protected EntityModel(TEntity entity) {
        this.entity = entity;
    }

    @Override
    public abstract String toString();

    protected abstract List<Function<TEntity, ?>> fields();

    @Override
    public int compareTo(TModel that) {
        return EntityComparator.compareBy(entity, that.entity, fields());
    }

    @Override
    public int hashCode() {
        return EntityComparator.hashCode(entity, fields());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityModel<?, ?> other && EntityComparator.equals(entity, other.entity, fields());
    }
}
