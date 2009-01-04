/* Copyright (c) 2001-2009, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.hsqldb.jdbc.testbase.BaseTestCase;

/**
 *
 * @author boucherb@users
 */
public class JDBCPreparedStatementTest extends BaseTestCase {

    private List<PreparedStatement> m_statementList;

    public JDBCPreparedStatementTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        m_statementList = new ArrayList<PreparedStatement>();

        super.setUp();

        executeScript("setup-all_types-table.sql");
    }

    @Override
    protected void tearDown() throws Exception {
        for(PreparedStatement pstmt : m_statementList) {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch(Exception e) {}
            }
        }

        super.tearDown();
    }

    protected PreparedStatement prepareStatement(String sql) throws Exception
    {
        PreparedStatement pstmt = newConnection().prepareStatement(sql);

        m_statementList.add(pstmt);

        return pstmt;
    }

    protected PreparedStatement queryBy(String column) throws Exception {
        String sql = "select * from all_types where " + column + " = ?";

        return prepareStatement(sql);
    }

    protected PreparedStatement updateColumnWhere(String updateColumn,
                                                        String whereColumn)
                                                        throws Exception {
        String sql = "update all_types set "
                    + updateColumn
                    + " = ? where "
                    + whereColumn
                    + " = ?";

        return prepareStatement(sql);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JDBCPreparedStatementTest.class);

        return suite;
    }

    /**
     * Test of setEscapeProcessing method, of interface java.sql.PreparedStatement.
     */
    public void testSetEscapeProcessing() throws Exception {
        println("setEscapeProcessing");

        boolean           enable = true;
        PreparedStatement stmt   = queryBy("id");

        try {
            stmt.setEscapeProcessing(enable);
            stmt.setInt(1, 1);

            assertTrue(stmt.executeQuery().next());
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }

        enable = false;
        stmt   = queryBy("id");

        try {
            stmt.setEscapeProcessing(enable);
            stmt.setInt(1, 1);

            assertTrue(stmt.executeQuery().next());
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of execute method, of interface java.sql.PreparedStatement.
     */
    public void testExecute() throws Exception {
        println("execute");

        boolean           expResult;
        boolean           result;
        PreparedStatement stmt = queryBy("id");

        stmt.setInt(1, 1);

        expResult = true;
        result    = stmt.execute();

        assertEquals(expResult, result);

        stmt.close();

        expResult = false;
        stmt      = updateColumnWhere("c_varchar", "id");

        stmt.setString(1, "Execute");
        stmt.setInt(2, 1);

        result = stmt.execute();

        assertEquals(expResult, result);

        stmt.close();
    }

    /**
     * Test of executeQuery method, of interface java.sql.PreparedStatement.
     */
    public void testExecuteQuery() throws Exception {
        println("executeQuery");

        PreparedStatement stmt = queryBy("id");

        stmt.setInt(1, 1);

        ResultSet rs = stmt.executeQuery();

        assertEquals(true, rs.next());

        stmt.close();
    }

    /**
     * Test of executeUpdate method, of interface java.sql.PreparedStatement.
     */
    public void testExecuteUpdate() throws Exception {
        println("executeUpdate");

        PreparedStatement stmt        = updateColumnWhere("c_varchar", "id");
        String            updateValue = "New Value";

        stmt.setString(1, updateValue);
        stmt.setInt(2, 1);

        int expResult = 1;
        int result    = stmt.executeUpdate();

        assertEquals(expResult, result);

        stmt = queryBy("id");

        stmt.setInt(1, 1);

        ResultSet rs = stmt.executeQuery();

        rs.next();

        assertEquals(updateValue, rs.getString("c_varchar"));
    }

    /**
     * Test of executeBatch method, of interface java.sql.PreparedStatement.
     */
    public void testExecuteBatch() throws Exception {
        println("executeBatch");

        PreparedStatement stmt = updateColumnWhere("c_varchar", "id");

        stmt.setString(1, "executeBatch");
        stmt.setInt(2, 1);

        stmt.addBatch();

        stmt.setString(1, "executeBatch Again");
        stmt.setInt(2, 1);

        stmt.addBatch();

        int[] expResult = new int[] {1 ,1};
        int[] result = stmt.executeBatch();

        assertEquals(expResult.length, result.length);

        for (int i = 0; i < result.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of setNull method, of interface java.sql.PreparedStatement.
     */
    public void testSetNull() throws Exception {
        println("setNull");

        PreparedStatement stmt = updateColumnWhere("c_integer", "id");

        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, 1);

        assertEquals(1, stmt.executeUpdate());
    }

    /**
     * Test of setBoolean method, of interface java.sql.PreparedStatement.
     */
    public void testSetBoolean() throws Exception {
        println("setBoolean");

        int parameterIndex = 1;
        boolean x = true;
        PreparedStatement stmt = queryBy("c_boolean");

        stmt.setBoolean(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setByte method, of interface java.sql.PreparedStatement.
     */
    public void testSetByte() throws Exception {
        println("setByte");

        int parameterIndex = 1;
        byte x = 0;
        PreparedStatement stmt = queryBy("c_tinyint");

        stmt.setByte(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setShort method, of interface java.sql.PreparedStatement.
     */
    public void testSetShort() throws Exception {
        println("setShort");

        int parameterIndex = 1;
        short x = 0;
        PreparedStatement stmt = queryBy("c_smallint");

        stmt.setShort(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setInt method, of interface java.sql.PreparedStatement.
     */
    public void testSetInt() throws Exception {
        println("setInt");

        int parameterIndex = 1;
        int x = 0;
        PreparedStatement stmt = queryBy("c_integer");

        stmt.setInt(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setLong method, of interface java.sql.PreparedStatement.
     */
    public void testSetLong() throws Exception {
        println("setLong");

        int parameterIndex = 1;
        long x = 0L;
        PreparedStatement stmt = queryBy("c_bigint");

        stmt.setLong(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setFloat method, of interface java.sql.PreparedStatement.
     */
    public void testSetFloat() throws Exception {
        println("setFloat");

        int parameterIndex = 1;
        float x = 0.0F;
        PreparedStatement stmt = queryBy("c_float");

        stmt.setFloat(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setDouble method, of interface java.sql.PreparedStatement.
     */
    public void testSetDouble() throws Exception {
        println("setDouble");

        int parameterIndex = 1;
        double x = 0.0;
        PreparedStatement stmt = queryBy("c_double");

        stmt.setDouble(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setBigDecimal method, of interface java.sql.PreparedStatement.
     */
    public void testSetBigDecimal() throws Exception {
        println("setBigDecimal");

        int parameterIndex = 1;
        BigDecimal x = null;
        PreparedStatement stmt = queryBy("c_decimal");

        stmt.setBigDecimal(parameterIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setString method, of interface java.sql.PreparedStatement.
     */
    public void testSetString() throws Exception {
        println("setString");

        int parameterIndex = 1;
        String x = "1";
        PreparedStatement stmt = queryBy("c_varchar");

        try {
            stmt.setString(parameterIndex, x);
            stmt.executeQuery();
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of setBytes method, of interface java.sql.PreparedStatement.
     */
    public void testSetBytes() throws Exception {
        println("setBytes");

        int paramIndex = 1;
        byte[] x = null;
        PreparedStatement stmt = queryBy("c_binary");

        stmt.setBytes(paramIndex, x);

        stmt.executeQuery();
    }

    /**
     * Test of setDate method, of interface java.sql.PreparedStatement.
     */
    public void testSetDate() throws Exception {
        println("setDate");

        Date              x    = Date.valueOf("2005-12-13");
        PreparedStatement stmt = queryBy("c_date");

        stmt.setDate(1, x);
        stmt.executeQuery();
    }

    /**
     * Test of setTime method, of interface java.sql.PreparedStatement.
     */
    public void testSetTime() throws Exception {
        println("setTime");

        Time x = Time.valueOf("11:23:02");
        PreparedStatement stmt = queryBy("c_time");

        stmt.setTime(1, x);

        stmt.executeQuery();
    }

    /**
     * Test of setTimestamp method, of interface java.sql.PreparedStatement.
     */
    public void testSetTimestamp() throws Exception {
        println("setTimestamp");

        Timestamp         x    = Timestamp.valueOf("2005-12-13 11:23:02.0123");
        PreparedStatement stmt = queryBy("c_timestamp");

        stmt.setTimestamp(1, x);
        stmt.executeQuery();
    }

    /**
     * Test of setAsciiStream method, of interface java.sql.PreparedStatement.
     */
    public void testSetAsciiStream() throws Exception {
        println("setAsciiStream");

        String            sval   = "setAsciiStream";
        byte[]            xval   = sval.getBytes("US-ASCII");
        InputStream       x      = new ByteArrayInputStream(xval);
        int               length = xval.length;
        PreparedStatement stmt   = updateColumnWhere("c_varchar", "id");

        stmt.setAsciiStream(1, x, length);
        stmt.setInt(2, 1);

        stmt.executeUpdate();

        stmt = queryBy("id");

        stmt.setInt(1, 1);

        ResultSet rs = stmt.executeQuery();

        rs.next();

        String result = rs.getString("c_varchar");

        assertEquals(sval, result);
    }

    /**
     * Test of setUnicodeStream method, of interface java.sql.PreparedStatement.
     */
    public void testSetUnicodeStream() throws Exception {
        println("setUnicodeStream");

        String            expVal = " setUnicodeStream";
        byte[]            xval   = expVal.getBytes("UTF8");
        InputStream       x      = new ByteArrayInputStream(xval);
        int               length = xval.length;
        PreparedStatement stmt   = updateColumnWhere("c_varchar", "id");

        stmt.setUnicodeStream(1, x, length);
        stmt.setInt(2, 1);

        assertEquals(1, stmt.executeUpdate());

        stmt = queryBy("id");

        stmt.setInt(1, 1);

        ResultSet rs = stmt.executeQuery();

        rs.next();

        String result = rs.getString("c_varchar");

        for (int i = 0; i < result.length(); i++) {
            assertEquals("at position " + i, (int)expVal.charAt(i), (int)result.charAt(i));
        }

        assertEquals(expVal.charAt(expVal.length() - 1), result.charAt(result.length() - 1));
        assertEquals("expVal.length(), result.length()", expVal.length(), result.length());
        assertEquals(expVal, result);
    }

    /**
     * Test of setBinaryStream method, of interface java.sql.PreparedStatement.
     */
    public void testSetBinaryStream() throws Exception {
        println("setBinaryStream");

        String            sval   = "setBinaryStream";
        byte[]            xval   = sval.getBytes("US-ASCII");
        InputStream       x      = new ByteArrayInputStream(xval);
        int               length = xval.length;
        PreparedStatement stmt   = updateColumnWhere("c_binary", "id");

        stmt.setBinaryStream(1, x, length);
        stmt.setInt(2, 1);

        assertEquals("stmt.executeUpdate()", 1, stmt.executeUpdate());

        stmt = queryBy("id");

        stmt.setInt(1, 1);

        ResultSet rs = stmt.executeQuery();

        assertEquals("rs.next()", true, rs.next());

        byte[] result = rs.getBytes("c_binary");

        assertJavaArrayEquals(xval, result);
    }

    /**
     * Test of clearParameters method, of interface java.sql.PreparedStatement.
     */
    public void testClearParameters() throws Exception {
        println("clearParameters");

        PreparedStatement stmt = queryBy("id");

        stmt.setInt(1, 1);

        stmt.executeQuery();

        stmt.clearParameters();

        try {
            stmt.executeQuery();
            fail("Allowed execute with cleared parameters");
        } catch (Exception ex) {

        }
    }

    void setObjectTest(String colName, Object x, int type) throws Exception {
        PreparedStatement stmt = queryBy(colName);

        stmt.setObject(1, x, type);
        stmt.executeQuery();
    }

    /**
     * Test of setObject method, of interface java.sql.PreparedStatement.
     */
    public void testSetObject() throws Exception {
        println("setObject");

        setObjectTest("c_bigint", new Long(Long.MAX_VALUE), Types.BIGINT);
        setObjectTest("c_binary", "setObject".getBytes(), Types.BINARY);
        setObjectTest("c_boolean",  Boolean.TRUE, Types.BOOLEAN);
        setObjectTest("c_char", "setObject  ", Types.CHAR);
        setObjectTest("c_date", Date.valueOf("2005-12-13"), Types.DATE);
        setObjectTest("c_decimal", new BigDecimal(1.123456789), Types.DECIMAL);
        setObjectTest("c_double", new Double(Double.MAX_VALUE), Types.DOUBLE);
        setObjectTest("c_float", new Double(Double.MAX_VALUE), Types.FLOAT);
        setObjectTest("c_integer", new Integer(Integer.MIN_VALUE), Types.INTEGER);
        setObjectTest("c_longvarbinary", "setObject".getBytes(), Types.LONGVARBINARY);
        setObjectTest("c_longvarchar", "setObject", Types.LONGVARCHAR);
        //
        setObjectTest("c_object", new boolean[10], Types.OTHER);
        setObjectTest("c_object", new byte[10], Types.OTHER);
        setObjectTest("c_object", new short[10], Types.OTHER);
        setObjectTest("c_object", new char[10], Types.OTHER);
        setObjectTest("c_object", new int[10], Types.OTHER);
        setObjectTest("c_object", new long[10], Types.OTHER);
        setObjectTest("c_object", new float[10], Types.OTHER);
        setObjectTest("c_object", new double[10], Types.OTHER);
        //
        setObjectTest("c_object", new String[10], Types.OTHER);
        setObjectTest("c_object", new Boolean[10], Types.OTHER);
        setObjectTest("c_object", new Byte[10], Types.OTHER);
        setObjectTest("c_object", new Short[10], Types.OTHER);
        setObjectTest("c_object", new Character[10], Types.OTHER);
        setObjectTest("c_object", new Integer[10], Types.OTHER);
        setObjectTest("c_object", new Long[10], Types.OTHER);
        setObjectTest("c_object", new Float[10], Types.OTHER);
        setObjectTest("c_object", new Double[10], Types.OTHER);
        //
        setObjectTest("c_real", new Float(Float.MAX_VALUE), Types.REAL);
        setObjectTest("c_smallint", new Short(Short.MAX_VALUE), Types.SMALLINT);
        setObjectTest("c_time", Time.valueOf("1:23:47"), Types.TIME);
        setObjectTest("c_timestamp", Timestamp.valueOf("2005-12-13 01:23:47.1234"), Types.TIMESTAMP);
        setObjectTest("c_tinyint", new Byte(Byte.MAX_VALUE), Types.TINYINT);
        setObjectTest("c_varbinary", "setObject".getBytes(), Types.VARBINARY);
        setObjectTest("c_varchar", "setObject", Types.VARCHAR);

    }

    /**
     * Test of addBatch method, of interface java.sql.PreparedStatement.
     */
    public void testAddBatch() throws Exception {
        println("addBatch");

        PreparedStatement stmt = updateColumnWhere("c_varchar", "id");

        try {
            stmt.addBatch();
            stmt.executeBatch();
            fail("Allowed addBatch() without any parameters set.");
        } catch (Exception ex) {

        }

        stmt = updateColumnWhere("c_varchar", "id");

        stmt.setString(1, "addBatch");

        try {
            stmt.addBatch();
            stmt.executeBatch();
            fail("Allowed addBatch() without all parameters set.");
        } catch (Exception ex) {

        }

        stmt = updateColumnWhere("c_varchar", "id");

        stmt.setString(1, "addBatch");
        stmt.setInt(2, 1);
        stmt.executeBatch();

        stmt = queryBy("id");

        stmt.setInt(1, 1);

        try {
            stmt.addBatch();
            stmt.executeBatch();
            fail("non-update prepared statement allows addBatch() and/or executeBatch()");
        } catch (Exception e) {

        }

        stmt = updateColumnWhere("c_varchar", "id");

        try {
            stmt.addBatch("update all_types set c_integer = 1");
            stmt.executeBatch();
            fail("prepared statement allows addBatch(java.lang.String) and/or executeBatch() after addBatch(java.lang.String)");
        } catch (Exception ex) {

        }
    }

    /**
     * Test of setCharacterStream method, of interface java.sql.PreparedStatement.
     */
    public void testSetCharacterStream() throws Exception {
        println("setCharacterStream");

        String            sval   = "setCharacterStream";
        Reader            reader = new StringReader(sval);
        int               length = sval.length();
        PreparedStatement stmt   = queryBy("c_longvarchar");

        stmt.setCharacterStream(1, reader, length);
        stmt.executeQuery();
    }

    /**
     * Test of setRef method, of interface java.sql.PreparedStatement.
     */
    public void testSetRef() throws Exception {
        println("setRef");

        if (!getBooleanProperty("test.types.ref", true))
        {
            return;
        }

        Ref               x    = null;
        PreparedStatement stmt = queryBy("c_object");

        try {
            stmt.setRef(1, x);
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }

        stmt.executeQuery();
    }

    /**
     * Test of setBlob method, of interface java.sql.PreparedStatement.
     */
    public void testSetBlob() throws Exception {
        println("setBlob");

        byte[]            xval = "setBlob".getBytes();
        Blob              x    = new JDBCBlob(xval);
        PreparedStatement stmt = queryBy("c_longvarbinary");

        stmt.setBlob(1, x);
        stmt.executeQuery();
    }

    /**
     * Test of setClob method, of interface java.sql.PreparedStatement.
     */
    public void testSetClob() throws Exception {
        println("setClob");

        String            xval = "setClob";
        Clob              x    = new JDBCClob(xval);
        PreparedStatement stmt = queryBy("c_longvarchar");

        stmt.setClob(1, x);
        stmt.executeQuery();
    }

    /**
     * Test of setArray method, of interface java.sql.PreparedStatement.
     */
    public void testSetArray() throws Exception {
        println("setArray");

        if (!getBooleanProperty("test.types.array", true))
        {
            return;
        }

        Array x = null;
        PreparedStatement stmt = queryBy("c_object");

        try {
            stmt.setArray(1, x);
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }

        stmt.executeQuery();
    }

    /**
     * Test of getMetaData method, of interface java.sql.PreparedStatement.
     */
    public void testGetMetaData() throws Exception {
        println("getMetaData");

        int expColCount = 21;
        PreparedStatement stmt = queryBy("id");
        ResultSetMetaData rsmd = stmt.getMetaData();

        try {
            int count = rsmd.getColumnCount();

            assertEquals("Column Count", expColCount, count);

            for (int i = 1; i <= count; i++) {
                rsmd.getCatalogName(i);
                rsmd.getColumnClassName(i);
                rsmd.getColumnDisplaySize(i);
                rsmd.getColumnLabel(i);
                rsmd.getColumnName(i);
                rsmd.getColumnType(i);
                rsmd.getColumnTypeName(i);
                rsmd.getPrecision(i);
                rsmd.getScale(i);
                rsmd.getTableName(i);
                rsmd.isAutoIncrement(i);
                rsmd.isCaseSensitive(i);
                rsmd.isCurrency(i);
                rsmd.isDefinitelyWritable(i);
                rsmd.isNullable(i);
                rsmd.isReadOnly(i);
                rsmd.isSearchable(i);
                rsmd.isSigned(i);
                rsmd.isWritable(i);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        stmt.close();

        // JDBC 4 clarifies that rsmd remains valid *after* the generating
        // connection/statement/result set is closed
        try {
            int count = rsmd.getColumnCount();

            for (int i = 1; i <= count; i++) {
                rsmd.getCatalogName(i);
                rsmd.getColumnClassName(i);
                rsmd.getColumnDisplaySize(i);
                rsmd.getColumnLabel(i);
                rsmd.getColumnName(i);
                rsmd.getColumnType(i);
                rsmd.getColumnTypeName(i);
                rsmd.getPrecision(i);
                rsmd.getScale(i);
                rsmd.getTableName(i);
                rsmd.isAutoIncrement(i);
                rsmd.isCaseSensitive(i);
                rsmd.isCurrency(i);
                rsmd.isDefinitelyWritable(i);
                rsmd.isNullable(i);
                rsmd.isReadOnly(i);
                rsmd.isSearchable(i);
                rsmd.isSigned(i);
                rsmd.isWritable(i);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        println(rsmd);
    }

    /**
     * Test of setURL method, of interface java.sql.PreparedStatement.
     */
    public void testSetURL() throws Exception {
        println("setURL");

        if (!getBooleanProperty("test.types.datalink", true))
        {
            return;
        }

        java.net.URL x = null;
        PreparedStatement stmt = queryBy("c_object");

        try {
            stmt.setURL(1, x);
        } catch (SQLException ex) {
            fail("TODO: " + ex.getMessage());
        }

        stmt.executeQuery();
    }

    /**
     * Test of getParameterMetaData method, of interface java.sql.PreparedStatement.
     */
    public void testGetParameterMetaData() throws Exception {
        println("getParameterMetaData");

        PreparedStatement stmt     = queryBy("id");
        ParameterMetaData psmd     = stmt.getParameterMetaData();
        int               expCount = 1;
        int               count    = psmd.getParameterCount();

        assertEquals(expCount, count);

        String expClassName = "java.lang.Integer";
        int    expMode      = ParameterMetaData.parameterModeIn;
        int    expType      = Types.INTEGER;
        String expTypeName  = "INTEGER";
        int    expPrecision = 10;
        int    expScale     = 0;

        for (int i = 1; i <= count; i++) {
            assertEquals(expClassName, psmd.getParameterClassName(i));
            assertEquals(expMode,      psmd.getParameterMode(i));
            assertEquals(expType,      psmd.getParameterType(i));
            assertEquals(expTypeName,  psmd.getParameterTypeName(i));
            assertEquals(expPrecision, psmd.getPrecision(i));
            assertEquals(expScale,     psmd.getScale(i));
        }
    }

    /**
     * Test of setRowId method, of interface java.sql.PreparedStatement.
     */
    public void testSetRowId() throws Exception {
        println("setRowId");

        if (!getBooleanProperty("test.types.rowid", true))
        {
            return;
        }

        RowId             x    = null;
        PreparedStatement stmt = queryBy("c_object");

        try {
            stmt.setRowId(1, x);
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of setNString method, of interface java.sql.PreparedStatement.
     */
    public void testSetNString() throws Exception {
        println("setNString");

        PreparedStatement stmt = queryBy("c_varchar");

        stmt.setNString(1, "setNString");

        stmt.executeQuery();
    }

    /**
     * Test of setNCharacterStream method, of interface java.sql.PreparedStatement.
     */
    public void testSetNCharacterStream() throws Exception {
        println("setNCharacterStream");

        String            sval   = "setNCharacterStream";
        Reader            value  = new StringReader(sval);
        long              length = sval.length();
        PreparedStatement stmt   = queryBy("c_longvarchar");

        stmt.setNCharacterStream(1, value, length);
        stmt.executeQuery();
    }

    /**
     * Test of setNClob method, of interface java.sql.PreparedStatement.
     */
    public void testSetNClob() throws Exception {
        println("setNClob");

        NClob             value = new JDBCNClob("setNClob");
        PreparedStatement stmt  = queryBy("c_longvarchar");

        stmt.setNClob(1, value);
        stmt.executeQuery();
    }

    /**
     * Test of setSQLXML method, of interface java.sql.PreparedStatement.
     */
    public void testSetSQLXML() throws Exception {
        println("setSQLXML");

        if (!getBooleanProperty("test.types.sqlxml", true))
        {
            return;
        }

        SQLXML            xmlObject = null;
        PreparedStatement stmt      = queryBy("c_object");

        try {
            stmt.setSQLXML(1, xmlObject);
        } catch (SQLException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of setPoolable method, of interface java.sql.PreparedStatement.
     */
    public void testSetPoolable() throws Exception {
        println("setPoolable");

        boolean           poolable = true;
        PreparedStatement stmt     = queryBy("id");

        stmt.setPoolable(poolable);

        boolean result = stmt.isPoolable();

        assertEquals(poolable, result);

        poolable = false;

        stmt.setPoolable(poolable);

        result = stmt.isPoolable();

        assertEquals(poolable, result);
    }

    /**
     * Test of isPoolable method, of interface java.sql.PreparedStatement.
     */
    public void testIsPoolable() throws Exception {
        println("isPoolable");

        PreparedStatement stmt = queryBy("id");

        boolean expResult = true;
        boolean result = stmt.isPoolable();

        assertEquals(expResult, result);
    }

    /**
     * Test of close method, of interface java.sql.PreparedStatement.
     */
    public void testClose() throws Exception {
        println("close");

        PreparedStatement stmt = queryBy("id");

        stmt.setInt(1, 1);

        stmt.close();

        try {
            stmt.execute();
            fail("prepared statement execute succeeds after close()");
        } catch (Exception ex) {}
    }

    public static void main(java.lang.String[] argList) {

        junit.textui.TestRunner.run(suite());
    }

}
