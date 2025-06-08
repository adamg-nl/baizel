module nl.adamg.baizel.internal.database {
    exports nl.adamg.baizel.internal.database;

    uses java.sql.Driver;

    requires nl.adamg.baizel.internal.common.annotations;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
}
