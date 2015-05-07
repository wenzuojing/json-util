# json-util
这是一json相关的工具包，不包含序列化及反序列化实现，你可以使用[fastjson](https://github.com/alibaba/fastjson)、[jackson](https://github.com/FasterXML/jackson)等库，目前实现常用的工具有：

* 格式化

```java
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
```
 

* 属性过滤

```java
        String json = "{\"name\":\"mi米\",\"age\":30,\"weight\":100.1,\"Children\":[\"a\",\"b\",\"c\"],\"address\":{ \"city\":\"GuangZhou\",\"pro\":\"GuangDong\"}}";

        Assert.assertEquals("{\"name\":\"mi米\",\"weight\":100.1,\"address\":{ \"city\":\"GuangZhou\" }",
                JsonUtils.removeProperty(json, new String[]{"name", "weight", "address.city"}, true));
```
