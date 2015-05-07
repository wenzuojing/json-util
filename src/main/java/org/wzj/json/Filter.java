package org.wzj.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.wzj.json.Scanner.*;

/**
 * Created by wens on 15-5-6.
 */
public class Filter {

    private Map<String, String> includes = new HashMap<String, String>();
    private Map<String, String> excludes = new HashMap<String, String>();

    public Filter(String[] includes, String[] excludes) {
        if (includes != null) {
            for (String p : includes) {
                int i = p.indexOf(".");
                while (i != -1) {
                    String s = p.substring(0, i);

                    this.includes.put(s, s);

                    i = p.indexOf(".", i + 1);

                }
                this.includes.put(p, p);
            }
        }

        if (excludes != null) {
            for (String p : excludes) {
                this.excludes.put(p, p);
            }
        }

    }


    public String doFilter(String src) {

        if (src == null) {
            return null;
        }

        Scanner scanner = new Scanner();

        int key_begin_index = -1;
        boolean scan_key = false;
        String key = null;


        Stack<String> visitKeys = new Stack<String>();

        StringBuilder out = new StringBuilder(src.length());

        boolean flag = false;

        for (int i = 0, len = src.length(); i < len; i++) {

            char c = src.charAt(i);

            int v = scanner.step(c);

            if (key != null && (v == SCAN_END_OBJECT || v == SCAN_OBJECT_VALUE)) {
                if (v == SCAN_END_OBJECT) {
                    key = visitKeys.pop();
                } else {
                    key = null;
                }

                if (flag) {
                    flag = false;
                    continue;
                }

            }

            if (key != null && v == SCAN_BEGIN_OBJECT) {
                visitKeys.push(key);
                key = null;
            }

            if (key == null && '"' == c && v == SCAN_BEGIN_LITERAL) {
                scan_key = true;
                key_begin_index = i;
                continue;
            }

            if (key == null && '"' == c && scan_key) {
                scan_key = false;
                key = src.substring(key_begin_index + 1, i);
                flag = filter(visitKeys, key);

                if (!flag) {
                    out.append(src.substring(key_begin_index, i + 1));
                }

                continue;
            }

            if (scan_key) continue;

            if (!flag) {
                out.append(c);
            }

        }


        String jsonStr = out.toString().replaceAll(",\\s*(?=})", " ");

        return jsonStr;
    }

    private boolean filter(Stack<String> visitKeys, String key) {

        StringBuilder sb = new StringBuilder(visitKeys.size() + 10);

        Iterator<String> iterator = visitKeys.iterator();

        while (iterator.hasNext()) {
            sb.append(iterator.next()).append(".");
        }


        sb.append(key);
        String s = sb.toString();

        if (includes.containsKey(s)) {
            return false;
        }

        if (excludes.containsKey(s)) {
            return true;
        }

        return includes.size() == 0 ? false : true;
    }


}
