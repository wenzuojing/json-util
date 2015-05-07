package org.wzj.json;

import java.io.IOException;

import static org.wzj.json.Scanner.*;

/**
 * Created by wens on 15-5-5.
 */
public class Formatter {

    private String prefix;
    private String indent;

    public Formatter(String prefix, String indent) {
        this.prefix = prefix;
        this.indent = indent;
        if (prefix == null) {
            prefix = "";
        }

        if (indent == null) {
            indent = "  ";
        }
    }

    public String doFormat(String src) {

        if (src == null) {
            return null;
        }

        StringBuilder out = new StringBuilder(src.length());

        boolean needIndent = false;

        int depth = 0;

        Scanner scanner = new Scanner();

        for (int i = 0, len = src.length(); i < len; i++) {

            char c = src.charAt(i);

            try {
                int v = scanner.step(c);
                if (v == SCAN_SKIP_SPACE) {
                    continue;
                }
                if (v == SCAN_ERROR) {
                    break;
                }
                if (needIndent && v != SCAN_END_OBJECT && v != SCAN_END_ARRAY) {
                    needIndent = false;
                    depth++;
                    newline(out, prefix, indent, depth);
                }

                if (v == SCAN_CONTINUE) {
                    out.append(c);
                    continue;
                }

                if (c == '{' || c == '[') {
                    needIndent = true;
                    out.append(c);
                } else if (c == ',') {
                    out.append(c);
                    newline(out, prefix, indent, depth);

                } else if (c == ':') {
                    out.append(c);
                    out.append(' ');

                } else if (c == '}' || c == ']') {
                    if (needIndent) {
                        needIndent = false;
                    } else {
                        depth--;
                        newline(out, prefix, indent, depth);
                    }
                    out.append(c);
                } else {
                    out.append(c);
                }
            } catch (Exception e) {
                throw new JsonException("Fail to format.", e);
            }
        }

        return out.toString();

    }

    private void newline(StringBuilder out, String prefix, String indent, int depth) throws IOException {
        out.append('\n');
        out.append(prefix);
        for (int i = 0; i < depth; i++) {
            out.append(indent);
        }
    }

}
