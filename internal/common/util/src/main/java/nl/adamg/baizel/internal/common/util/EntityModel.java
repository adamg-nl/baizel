package nl.adamg.baizel.internal.common.util;

public abstract class EntityModel<TEntity> {
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

    public TEntity entity() {
        return entity;
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityModel<?> other && entity.equals(other.entity);
    }
}
