package be.wegenenverkeer.atomium.store;

import be.wegenenverkeer.atomium.api.Codec;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Karel Maesen, Geovise BVBA on 10/12/16.
 */
public interface JdbcDialect {

    public JdbcTotalSizeOp createTotalSizeOp(Connection conn, JdbcEntryStoreMetadata meta);

    public <T> JdbcSaveEntryOp<T> createSaveEntryOp(Connection conn, Codec<T, String> codec, JdbcEntryStoreMetadata meta) throws SQLException;

    public <T> JdbcGetEntriesOp<T> createGetEntriesOp(Connection conn, Codec<T, String> codec, JdbcEntryStoreMetadata meta);

    public JdbcCreateTablesOp createEntryTable(Connection conn, JdbcEntryStoreMetadata meta);


}