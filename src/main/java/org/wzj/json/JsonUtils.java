package org.wzj.json;

/**
 * Created by wens on 15-5-7.
 */
public class JsonUtils {

    /**
     * 格式化json
     *
     * @param src
     * @return
     */
    public static String format(String src) {
        return new Formatter("", "  ").doFormat(src);
    }

    /**
     * 删除属性
     *
     * @param src
     * @param poperties
     * @param including
     * @return
     */
    public static String removeProperty(String src, String[] poperties, boolean including) {

        Filter filter = null;

        if (including) {
            filter = new Filter(poperties, null);
        } else {
            filter = new Filter(null, poperties);
        }

        return filter.doFilter(src);

    }


    /**
     * 删除属性
     *
     * @param src
     * @param poperties
     * @return
     */
    public static String removeProperty(String src, String[] poperties) {

        return removeProperty(src, poperties, true);

    }

}
