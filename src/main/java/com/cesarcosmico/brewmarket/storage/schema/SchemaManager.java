package com.cesarcosmico.brewmarket.storage.schema;

import java.sql.Connection;
import java.sql.SQLException;

public interface SchemaManager {

    void createSchema(Connection connection) throws SQLException;
}
