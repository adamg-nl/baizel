module com.foobar.billing {
    exports com.foobar.billing;

    requires com.foobar.common;
    requires jsr305;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.core;
}
