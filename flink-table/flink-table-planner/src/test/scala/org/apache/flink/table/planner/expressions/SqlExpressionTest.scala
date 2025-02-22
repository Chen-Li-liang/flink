/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.planner.expressions

import org.apache.flink.api.java.typeutils.RowTypeInfo
import org.apache.flink.table.planner.codegen.CodeGenException
import org.apache.flink.table.planner.expressions.utils.ExpressionTestBase
import org.apache.flink.types.Row

import org.junit.Test

/**
  * Tests all SQL expressions that are currently supported according to the documentation.
  * This tests should be kept in sync with the documentation to reduce confusion due to the
  * large amount of SQL functions.
  *
  * The tests do not test every parameter combination of a function.
  * They are rather a function existence test and simple functional test.
  *
  * The tests are split up and ordered like the sections in the documentation.
  */
class SqlExpressionTest extends ExpressionTestBase {

  @Test
  def testComparisonFunctions(): Unit = {
    testSqlApi("1 = 1", "true")
    testSqlApi("1 <> 1", "false")
    testSqlApi("5 > 2", "true")
    testSqlApi("2 >= 2", "true")
    testSqlApi("5 < 2", "false")
    testSqlApi("2 <= 2", "true")
    testSqlApi("x'0c' <= x'0b'", "false")
    testSqlApi("x'0c' > x'0b'", "true")
    testSqlApi("x'0c' = x'0c'", "true")
    testSqlApi("x'0c' <> x'0c'", "false")
    testSqlApi("1 IS NULL", "false")
    testSqlApi("1 IS NOT NULL", "true")
    testSqlApi("NULLIF(1,1) IS DISTINCT FROM NULLIF(1,1)", "false")
    testSqlApi("NULLIF(1,1) IS NOT DISTINCT FROM NULLIF(1,1)", "true")
    testSqlApi("NULLIF(1,1) IS NOT DISTINCT FROM NULLIF(1,1)", "true")
    testSqlApi("12 BETWEEN 11 AND 13", "true")
    testSqlApi("12 BETWEEN ASYMMETRIC 13 AND 11", "false")
    testSqlApi("12 BETWEEN SYMMETRIC 13 AND 11", "true")
    testSqlApi("12 NOT BETWEEN 11 AND 13", "false")
    testSqlApi("12 NOT BETWEEN ASYMMETRIC 13 AND 11", "true")
    testSqlApi("12 NOT BETWEEN SYMMETRIC 13 AND 11", "false")
    testSqlApi("'TEST' LIKE '%EST'", "true")
    testSqlApi("'%EST' LIKE '.%EST' ESCAPE '.'", "true")
    testSqlApi("'TEST' NOT LIKE '%EST'", "false")
    testSqlApi("'%EST' NOT LIKE '.%EST' ESCAPE '.'", "false")
    testSqlApi("'TEST' SIMILAR TO '.EST'", "true")
    //testSqlApi("'TEST' SIMILAR TO ':.EST' ESCAPE ':'", "true") // TODO
    testSqlApi("'TEST' NOT SIMILAR TO '.EST'", "false")
    //testSqlApi("'TEST' NOT SIMILAR TO ':.EST' ESCAPE ':'", "false") // TODO
    testSqlApi("'TEST' IN ('west', 'TEST', 'rest')", "true")
    testSqlApi("'TEST' IN ('west', 'rest')", "false")
    testSqlApi("'TEST' NOT IN ('west', 'TEST', 'rest')", "false")
    testSqlApi("'TEST' NOT IN ('west', 'rest')", "true")

    // sub-query functions are not listed here
  }

  @Test
  def testValidImplicitTypeConversion: Unit = {
    // implicit type conversion between tinyint and others
    testSqlApi("cast(1 as tinyint) = cast(1 as smallint)","true")
    testSqlApi("cast(1 as tinyint) = cast(1 as int)","true")
    testSqlApi("cast(1 as tinyint) = cast(1 as bigint)","true")
    testSqlApi("cast(1 as tinyint) = cast(1 as decimal)","true")
    testSqlApi("cast(1 as tinyint) = cast(1 as float)","true")
    testSqlApi("cast(1 as tinyint) = cast(1 as double)","true")
    testSqlApi("cast(1 as tinyint) = cast(2 as int)","false")
    testSqlApi("cast(1 as tinyint) = cast(2 as bigint)","false")
    testSqlApi("cast(1 as tinyint) = cast(2 as decimal)","false")
    testSqlApi("cast(1 as tinyint) = cast(2.1 as float)","false")
    testSqlApi("cast(1 as tinyint) = cast(2.1 as double)","false")

    // implicit type conversion between smallint and others
    testSqlApi("cast(1 as smallint) = cast(1 as int)","true")
    testSqlApi("cast(1 as smallint) = cast(1 as bigint)","true")
    testSqlApi("cast(1 as smallint) = cast(1 as decimal)","true")
    testSqlApi("cast(1 as smallint) = cast(1 as float)","true")
    testSqlApi("cast(1 as smallint) = cast(1 as double)","true")
    testSqlApi("cast(1 as smallint) = cast(2 as int)","false")
    testSqlApi("cast(1 as smallint) = cast(2 as bigint)","false")
    testSqlApi("cast(1 as smallint) = cast(2 as decimal)","false")
    testSqlApi("cast(1 as smallint) = cast(2.1 as float)","false")
    testSqlApi("cast(1 as smallint) = cast(2.1 as double)","false")

    // implicit type conversion between int and others
    testSqlApi("cast(1 as int) = cast(1 as bigint)","true")
    testSqlApi("cast(1 as int) = cast(1 as decimal)","true")
    testSqlApi("cast(1 as int) = cast(1 as float)","true")
    testSqlApi("cast(1 as int) = cast(1 as double)","true")
    testSqlApi("cast(1 as int) = cast(2 as bigint)","false")
    testSqlApi("cast(1 as int) = cast(2 as decimal)","false")
    testSqlApi("cast(1 as int) = cast(2.1 as float)","false")
    testSqlApi("cast(1 as int) = cast(2.1 as double)","false")

    // implicit type conversion between bigint and others
    testSqlApi("cast(1 as bigint) = cast(1 as decimal)","true")
    testSqlApi("cast(1 as bigint) = cast(1 as float)","true")
    testSqlApi("cast(1 as bigint) = cast(1 as double)","true")
    testSqlApi("cast(1 as bigint) = cast(2 as decimal)","false")
    testSqlApi("cast(1 as bigint) = cast(2.1 as float)","false")
    testSqlApi("cast(1 as bigint) = cast(2.1 as double)","false")

    // implicit type conversion between decimal and others
    testSqlApi("cast(1 as decimal) = cast(1 as float)","true")
    testSqlApi("cast(1 as decimal) = cast(1 as double)","true")
    testSqlApi("cast(1 as decimal) = cast(2.1 as float)","false")
    testSqlApi("cast(1 as decimal) = cast(2.1 as double)","false")

    // implicit type conversion between float and others
    testSqlApi("cast(1 as float) = cast(1 as double)","true")
    testSqlApi("cast(1 as float) = cast(2.1 as double)","false")

    // implicit type conversion between date and varchar
    testSqlApi("cast('2001-01-01' as date) = cast('2001-01-01' as varchar)","true")
    testSqlApi("cast('2001-01-01' as date) = cast('2001-01-02' as varchar)","false")

    // implicit type conversion between date and char
    testSqlApi("cast('2001-01-01' as date) = cast('2001-01-01' as char)","true")
    testSqlApi("cast('2001-01-01' as date) = cast('2001-01-02' as char)","false")

    // implicit type conversion between time and char
    testSqlApi("cast('00:00:00.000000000' as time) = cast('00:00:00.000000000' as varchar)","true")
    testSqlApi("cast('00:00:00.000000000' as time) = cast('00:00:01.000000000' as varchar)","false")

    // implicit type conversion between time and char
    testSqlApi("cast('2001-01-01 12:12:12.123' as timestamp)" +
      " = cast('2001-01-01 12:12:12.1230' as varchar)","true")
    testSqlApi("cast('1990-10-14 12:12:12.123' as timestamp)" +
      " = cast('1990-10-15 12:12:12.123' as varchar)","false")
  }

  @Test
  def testInValidImplicitTypeConversion: Unit = {
    val typeChar = "CHAR"
    val typeVarChar = "VARCHAR"

    val typeTinyInt = "TINYINT"
    val typeSmallInt = "SMALLINT"
    val typeInt = "INTEGER"
    val typeBigInt = "BIGINT"
    val typeDecimal = "DECIMAL"
    val typeFloat = "FLOAT"
    val typeDouble = "DOUBLE"

    // implicit type conversion between char and others
    testInvalidImplicitConversionTypes(
      "'1' = cast(1 as tinyint)",
      List(typeChar,typeTinyInt))

    testInvalidImplicitConversionTypes(
      "'1' = cast(1 as smallint)",
      List(typeChar,typeSmallInt))

    testInvalidImplicitConversionTypes(
      "'1' = cast(1 as int)",
      List(typeChar,typeInt))

    testInvalidImplicitConversionTypes(
      "'1' = cast(1 as bigint)",
      List(typeChar,typeBigInt))

    testInvalidImplicitConversionTypes(
      "'1.1' = cast(1.1 as decimal(2, 1))",
      List(typeChar,typeDecimal))

    testInvalidImplicitConversionTypes(
      "'1.1' = cast(1.1 as float)",
      List(typeChar,typeFloat))

    testInvalidImplicitConversionTypes(
      "'1.1' = cast(1.1 as double)",
      List(typeChar,typeDouble))

    // implicit type conversion between varchar and others
    testInvalidImplicitConversionTypes(
      "cast('1' as varchar) = cast(1 as tinyint)",
      List(typeVarChar,typeTinyInt))

    testInvalidImplicitConversionTypes(
      "cast('1' as varchar) = cast(1 as smallint)",
      List(typeVarChar,typeSmallInt))

    testInvalidImplicitConversionTypes(

      "cast('1' as varchar) = cast(1 as int)",
      List(typeVarChar,typeInt))

    testInvalidImplicitConversionTypes(
      "cast('1' as varchar) = cast(1 as bigint)",
      List(typeVarChar,typeBigInt))

    testInvalidImplicitConversionTypes(
      "cast('1.1' as varchar) = cast(1.1 as decimal(2, 1))",
      List(typeVarChar,typeDecimal))

    testInvalidImplicitConversionTypes(
      "cast('1.1' as varchar) = cast(1.1 as float)",
      List(typeVarChar,typeFloat))

    testInvalidImplicitConversionTypes(
      "cast('1.1' as varchar) = cast(1.1 as double)",
      List(typeVarChar,typeDouble))
  }

  private def testInvalidImplicitConversionTypes(sql: String, types: List[String]): Unit ={
    val expectedExceptionMessage =
      "implicit type conversion between " +
      s"${types(0)}" +
      s" and " +
      s"${types(1)}" +
      s" is not supported now"
    testExpectedSqlException(sql, expectedExceptionMessage, classOf[CodeGenException])
  }

  @Test
  def testLogicalFunctions(): Unit = {
    testSqlApi("TRUE OR FALSE", "true")
    testSqlApi("TRUE AND FALSE", "false")
    testSqlApi("NOT TRUE", "false")
    testSqlApi("TRUE IS FALSE", "false")
    testSqlApi("TRUE IS NOT FALSE", "true")
    testSqlApi("TRUE IS TRUE", "true")
    testSqlApi("TRUE IS NOT TRUE", "false")
    testSqlApi("NULLIF(TRUE,TRUE) IS UNKNOWN", "true")
    testSqlApi("NULLIF(TRUE,TRUE) IS NOT UNKNOWN", "false")
  }

  @Test
  def testArithmeticFunctions(): Unit = {
    testSqlApi("+5", "5")
    testSqlApi("-5", "-5")
    testSqlApi("5+5", "10")
    testSqlApi("5-5", "0")
    testSqlApi("5*5", "25")
    testSqlApi("5/5", "1")
    testSqlApi("5%2", "1");
    testSqlApi("POWER(5, 5)", "3125.0")
    testSqlApi("ABS(-5)", "5")
    testSqlApi("MOD(-26, 5)", "-1")
    testSqlApi("SQRT(4)", "2.0")
    testSqlApi("LN(1)", "0.0")
    testSqlApi("LOG10(1)", "0.0")
    testSqlApi("EXP(0)", "1.0")
    testSqlApi("CEIL(2.5)", "3")
    testSqlApi("CEILING(2.5)", "3")
    testSqlApi("FLOOR(2.5)", "2")
    testSqlApi("SIN(2.5)", "0.5984721441039564")
    testSqlApi("SINH(2.5)", "6.0502044810397875")
    testSqlApi("COS(2.5)", "-0.8011436155469337")
    testSqlApi("TAN(2.5)", "-0.7470222972386603")
    testSqlApi("COT(2.5)", "-1.3386481283041514")
    testSqlApi("ASIN(0.5)", "0.5235987755982989")
    testSqlApi("ACOS(0.5)", "1.0471975511965979")
    testSqlApi("ATAN(0.5)", "0.4636476090008061")
    testSqlApi("ATAN2(0.5, 0.5)", "0.7853981633974483")
    testSqlApi("COSH(2.5)", "6.132289479663686")
    testSqlApi("TANH(2.5)", "0.9866142981514303")
    testSqlApi("DEGREES(0.5)", "28.64788975654116")
    testSqlApi("RADIANS(0.5)", "0.008726646259971648")
    testSqlApi("SIGN(-1.1)", "-1.0")  // calcite: SIGN(Decimal(p,s)) => Decimal(p,s)
    testSqlApi("ROUND(-12.345, 2)", "-12.35")
    testSqlApi("PI()", "3.141592653589793")
    testSqlApi("E()", "2.718281828459045")
    testSqlApi("truncate(42.345)", "42")
    testSqlApi("truncate(cast(42.345 as decimal(5, 3)), 2)", "42.34")
  }

  @Test
  def testDivideFunctions(): Unit = {

    //slash

    // Decimal(2,1) / Decimal(2,1) => Decimal(8,6)
    testSqlApi("1.0/8.0", "0.125000")
    testSqlApi("2.0/3.0", "0.666667")

    // Integer => Decimal(10, 0)
    // Decimal(10,0) / Decimal(2,1) => Decimal(17,6)
    testSqlApi("-2/3.0", "-0.666667")

    // Decimal(2,1) / Decimal(10,0) => Decimal(23,12)
    testSqlApi("2.0/(-3)", "-0.666666666667")
    testSqlApi("-7.9/2", "-3.950000000000")

    // invalid division
    val divisorZeroException = "Division by zero"
    testExpectedSqlException(
      "1/cast(0.00 as decimal)", divisorZeroException, classOf[ArithmeticException])
    testExpectedSqlException(
      "1/cast(0.00 as double)", divisorZeroException, classOf[ArithmeticException])
    testExpectedSqlException(
      "1/cast(0.00 as float)", divisorZeroException, classOf[ArithmeticException])
    testExpectedSqlException(
      "1/cast(0 as tinyint)", divisorZeroException, classOf[ArithmeticException])
    testExpectedSqlException(
      "1/cast(0 as smallint)", divisorZeroException, classOf[ArithmeticException])
    testExpectedSqlException(
      "1/0", divisorZeroException, classOf[ArithmeticException])
    testExpectedSqlException(
      "1/cast(0 as bigint)", divisorZeroException, classOf[ArithmeticException])
  }

  @Test
  def testStringFunctions(): Unit = {
    testSqlApi("'test' || 'string'", "teststring")
    testSqlApi("CHAR_LENGTH('string')", "6")
    testSqlApi("CHARACTER_LENGTH('string')", "6")
    testSqlApi("UPPER('string')", "STRING")
    testSqlApi("LOWER('STRING')", "string")
    testSqlApi("POSITION('STR' IN 'STRING')", "1")
    testSqlApi("TRIM(BOTH ' STRING ')", "STRING")
    testSqlApi("TRIM(LEADING 'x' FROM 'xxxxSTRINGxxxx')", "STRINGxxxx")
    testSqlApi("TRIM(TRAILING 'x' FROM 'xxxxSTRINGxxxx')", "xxxxSTRING")
    testSqlApi(
      "OVERLAY('This is a old string' PLACING 'new' FROM 11 FOR 3)",
      "This is a new string")
    testSqlApi("SUBSTRING('hello world', 2)", "ello world")
    testSqlApi("SUBSTRING('hello world', 2, 3)", "ell")
    testSqlApi("SUBSTRING('hello world', 2, 300)", "ello world")
    testSqlApi("SUBSTR('hello world', 2, 3)", "ell")
    testSqlApi("SUBSTR('hello world', 2)", "ello world")
    testSqlApi("SUBSTR('hello world', 2, 300)", "ello world")
    testSqlApi("SUBSTR('hello world', 0, 3)", "hel")
    testSqlApi("INITCAP('hello world')", "Hello World")
    testSqlApi("REGEXP_REPLACE('foobar', 'oo|ar', '')", "fb")
    testSqlApi("REGEXP_EXTRACT('foothebar', 'foo(.*?)(bar)', 2)", "bar")
    testSqlApi(
      "REPEAT('This is a test String.', 2)",
      "This is a test String.This is a test String.")
    testSqlApi("REPLACE('hello world', 'world', 'flink')", "hello flink")
  }

  @Test
  def testConditionalFunctions(): Unit = {
    testSqlApi("CASE 2 WHEN 1, 2 THEN 2 ELSE 3 END", "2")
    testSqlApi("CASE WHEN 1 = 2 THEN 2 WHEN 1 = 1 THEN 3 ELSE 3 END", "3")
    testSqlApi("NULLIF(1, 1)", "null")
    testSqlApi("COALESCE(NULL, 5)", "5")
  }

  @Test
  def testTypeConversionFunctions(): Unit = {
    testSqlApi("CAST(2 AS DOUBLE)", "2.0")
  }

  @Test
  def testValueConstructorFunctions(): Unit = {
    testSqlApi("ROW('hello world', 12)", "(hello world, 12)")
    testSqlApi("('hello world', 12)", "(hello world, 12)")
    testSqlApi("('foo', ('bar', 12))", "(foo, (bar, 12))")
    testSqlApi("ARRAY[TRUE, FALSE][2]", "false")
    testSqlApi("ARRAY[TRUE, TRUE]", "[true, true]")
    testSqlApi("MAP['k1', 'v1', 'k2', 'v2']['k2']", "v2")
    testSqlApi("MAP['k1', CAST(true AS VARCHAR(256)), 'k2', 'foo']['k1']", "true")
  }

  @Test
  def testDateTimeFunctions(): Unit = {
    testSqlApi("DATE '1990-10-14'", "1990-10-14")
    testSqlApi("TIME '12:12:12'", "12:12:12")
    testSqlApi("TIMESTAMP '1990-10-14 12:12:12.123'", "1990-10-14 12:12:12.123")
    testSqlApi("INTERVAL '10 00:00:00.004' DAY TO SECOND", "+10 00:00:00.004")
    testSqlApi("INTERVAL '10 00:12' DAY TO MINUTE", "+10 00:12:00.000")
    testSqlApi("INTERVAL '2-10' YEAR TO MONTH", "+2-10")
    testSqlApi("EXTRACT(DAY FROM DATE '1990-12-01')", "1")
    testSqlApi("EXTRACT(DAY FROM INTERVAL '19 12:10:10.123' DAY TO SECOND(3))", "19")
    testSqlApi("FLOOR(TIME '12:44:31' TO MINUTE)", "12:44:00")
    testSqlApi("CEIL(TIME '12:44:31' TO MINUTE)", "12:45:00")
    testSqlApi("QUARTER(DATE '2016-04-12')", "2")
    testSqlApi(
      "(TIME '2:55:00', INTERVAL '1' HOUR) OVERLAPS (TIME '3:30:00', INTERVAL '2' HOUR)",
      "true")
  }

  @Test
  def testArrayFunctions(): Unit = {
    testSqlApi("CARDINALITY(ARRAY[TRUE, TRUE, FALSE])", "3")
    testSqlApi("ELEMENT(ARRAY['HELLO WORLD'])", "HELLO WORLD")
  }

  @Test
  def testHashFunctions(): Unit = {
    testSqlApi("MD5('')", "d41d8cd98f00b204e9800998ecf8427e")
    testSqlApi("MD5('test')", "098f6bcd4621d373cade4e832627b4f6")

    testSqlApi("SHA1('')", "da39a3ee5e6b4b0d3255bfef95601890afd80709")
    testSqlApi("SHA1('test')", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")

    testSqlApi("SHA224('')", "d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f")
    testSqlApi("SHA2('', 224)", "d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f")

    testSqlApi("SHA224('test')", "90a3ed9e32b2aaf4c61c410eb925426119e1a9dc53d4286ade99a809")
    testSqlApi("SHA2('test', 224)", "90a3ed9e32b2aaf4c61c410eb925426119e1a9dc53d4286ade99a809")

    testSqlApi("SHA256('')", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    testSqlApi("SHA2('', 256)", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")

    testSqlApi("SHA256('test')", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
    testSqlApi("SHA2('test', 256)",
      "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")

    testSqlApi("SHA384('')", "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc" +
      "7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b")
    testSqlApi("SHA2('', 384)", "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0" +
      "cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b")

    testSqlApi("SHA384('test')", "768412320f7b0aa5812fce428dc4706b3cae50e02a64caa16a782249bfe8efc" +
      "4b7ef1ccb126255d196047dfedf17a0a9")
    testSqlApi("SHA2('test', 384)", "768412320f7b0aa5812fce428dc4706b3cae50e02a64caa16a782249bfe8" +
      "efc4b7ef1ccb126255d196047dfedf17a0a9")

    testSqlApi("SHA512('')", "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d" +
      "0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e")
    testSqlApi("SHA2('',512)", "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce4" +
      "7d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e")

    testSqlApi("SHA512('test')", "ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db" +
      "27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff")
    testSqlApi("SHA2('test',512)", "ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0" +
      "db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff")

    testSqlApi("MD5(CAST(NULL AS VARCHAR))", "null")
    testSqlApi("SHA1(CAST(NULL AS VARCHAR))", "null")
    testSqlApi("SHA224(CAST(NULL AS VARCHAR))", "null")
    testSqlApi("SHA256(CAST(NULL AS VARCHAR))", "null")
    testSqlApi("SHA384(CAST(NULL AS VARCHAR))", "null")
    testSqlApi("SHA512(CAST(NULL AS VARCHAR))", "null")
    testSqlApi("SHA2(CAST(NULL AS VARCHAR), 256)", "null")
  }

  @Test
  def testNullableCases(): Unit = {
    testSqlApi(
      "TO_BASE64(FROM_BASE64(cast(NUll as varchar)))",
      nullable
    )

    testSqlApi(
      "FROM_BASE64(cast(NUll as varchar))",
      nullable
    )
  }

  override def testData: Row = new Row(0)

  override def typeInfo: RowTypeInfo = new RowTypeInfo()
}
