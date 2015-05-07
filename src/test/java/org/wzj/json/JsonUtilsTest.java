package org.wzj.json;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by wens on 15-5-7.
 */
public class JsonUtilsTest extends TestCase {

    public void testFormat() {

        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

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

        Assert.assertEquals(expect, JsonUtils.format(json));

    }


    public void testRemoveProperty_0() {

        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

        Assert.assertEquals("{\"name\":\"mi米\",\"weight\":100.1,\"address\":{ \"city\":\"GuangZhou\" }",
                JsonUtils.removeProperty(json, new String[]{"name", "weight", "address.city"}, true));
    }

    public void testRemoveProperty_1() {

        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

        Assert.assertEquals("{\"name\":\"mi米\",\"weight\":100.1,\"address\":{ \"city\":\"GuangZhou\" }",
                JsonUtils.removeProperty(json, new String[]{"age", "Children", "address.pro"}, false));
    }

}
