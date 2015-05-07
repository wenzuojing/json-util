package org.wzj.json;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by wens on 15-5-7.
 */
public class FormatTest extends TestCase {


    public void test_0() {

        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

        Formatter formatter = new Formatter("", "   ");
        String json2 = formatter.doFormat(json);

        String expect = "{\n" +
                "   \"name\": \"mi米\",\n" +
                "   \"age\": 30,\n" +
                "   \"weight\": 100.1,\n" +
                "   \"Children\": [\n" +
                "      \"a\",\n" +
                "      \"b\",\n" +
                "      \"c\"\n" +
                "   ],\n" +
                "   \"address\": {\n" +
                "      \"city\": \"GuangZhou\",\n" +
                "      \"pro\": \"GuangDong\"\n" +
                "   }\n" +
                "}";

        Assert.assertEquals(expect, json2);

    }
}
