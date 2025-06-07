package nl.adamg.baizel.core;

public class Target {
    private final nl.adamg.baizel.core.entities.Target entity;

    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (entity.organization != null) {
            sb.append('@').append(entity.organization);
            if (entity.module != null) {
                sb.append('/').append(entity.module);
            }
        }
        sb.append("//").append(entity.path);
        if (entity.targetName != null) {
            sb.append(':').append(entity.targetName);
        }
        return sb.toString();
    }

    //region generated code
    public Target(nl.adamg.baizel.core.entities.Target entity) {
        this.entity = entity;
    }
    //endregion
}
