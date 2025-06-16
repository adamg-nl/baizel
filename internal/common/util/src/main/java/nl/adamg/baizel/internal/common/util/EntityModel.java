package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import java.util.List;
import java.util.function.Function;

/// Provides canonical implementation of methods relevant for value-like / primitive types:
/// equals, hashCode, compareTo, auto-implemented based on given list of fields.
/// Useful for model types (business logic classes) that wrap entity types (pure data structures).
/// @param <TEntity> entity being wrapped and modelled.
public abstract class EntityModel<TInterface, TEntity, TModel extends EntityModel<TInterface, TEntity, TModel>> implements Comparable<TInterface> {
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

    public TEntity entity() {
        return entity;
    }

    @Override
    public int compareTo(TInterface that) {
        @SuppressWarnings("unchecked")
        var cast = (TModel)that;
        return EntityComparator.compareBy(entity, cast.entity, fields());
    }

    @Override
    public int hashCode() {
        return EntityComparator.hashCode(entity, fields());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityModel<?, ?, ?> other &&
                entity.getClass() == other.entity.getClass() &&
                EntityComparator.equals(entity, other.entity, fields());
    }
}
