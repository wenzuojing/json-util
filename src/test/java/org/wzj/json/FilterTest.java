package org.wzj.json;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by wens on 15-5-7.
 */
public class FilterTest extends TestCase {

    public void test_0() {

        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

        Filter filter = new Filter(new String[]{"name", "weight", "address.city"}, null);

        String json2 = filter.doFilter(json);

        Assert.assertEquals("{\"name\":\"mi米\",\"weight\":100.1,\"address\":{ \"city\":\"GuangZhou\" }", json2);

    }

    public void test_1() {

        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

        Filter filter = new Filter(null, new String[]{"age", "Children", "address.pro"});

        String json2 = filter.doFilter(json);

        Assert.assertEquals("{\"name\":\"mi米\",\"weight\":100.1,\"address\":{ \"city\":\"GuangZhou\" }", json2);

    }
}
