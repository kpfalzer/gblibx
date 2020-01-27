/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2020 kpfalzer.
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 *
 */

package gblibx.yaap;

import java.util.*;

import static gblibx.Util.invariant;
import static gblibx.Util.repeat;

/**
 * Group of Option.
 */
public class Group {
    public Group(String description) {
        this.description = description;
    }

    public Group(String description, Option[] opts) {
        this(description);
        add(opts);
    }

    public static Group create(String description) {
        return new Group(description);
    }

    public Group add(Option opt) {
        String optNm = opt.optNm();
        invariant(!__opts.containsKey(optNm));
        __opts.put(optNm, opt);
        return this;
    }

    public Group add(Option[] opts) {
        for (Option opt : opts) {
            add(opt);
        }
        return this;
    }

    public Group add(String optNm, String description) {
        return add(new Option(optNm, description));
    }

    /**
     * Add optional ('?') option.
     * @param optNm option names.
     * @param argNm argument name.
     * @param description description.
     * @return this Group.
     */
    public Group add(String optNm, String argNm, String description) {
        return add(new Option(optNm, argNm, description));
    }

    /**
     * Short usage.
     *
     * @return short usage.
     */
    public String toString() {
        StringBuilder usage = new StringBuilder();
        for (String key : __opts.keySet()) {
            if (0 < usage.length()) usage.append(' ');
            usage.append(__opts.get(key).toString());
        }
        return usage.toString();
    }

    /**
     * Get detailed usage.
     *
     * @return detailed usage.
     */
    public String getDetailedUsage() {
        StringBuilder buf = new StringBuilder();
        String under = repeat("-", description.length());
        buf.append(description).append("\n").append(under).append("\n");
        int maxWidth = 0;
        for (int pass = 1; pass <= 2; ++pass) {
            for (String key : __opts.keySet()) {
                if (1 == pass) {
                    int w = __opts.get(key).getDetailedArgs().length();
                    if (w > maxWidth) maxWidth = w;
                } else {
                    buf.append(__opts.get(key).getDetailedUsage(maxWidth)).append("\n");
                }
            }
            if (1 == pass) maxWidth += 2;
        }
        return buf.toString();
    }

    public boolean containsKey(String optNm) {
        return __opts.containsKey(optNm);
    }

    public Option get(String optNm) {
        invariant(containsKey(optNm));
        return __opts.get(optNm);
    }

    public Collection<Option> allOptions() {
        return __opts.values();
    }

    /**
     * Look for raw option name.
     *
     * @param optNm option name.
     * @return option matching name or null if not found.
     */
    public Option find(String optNm) {
        for (Option option : allOptions()) {
            for (String nm : option.optNm) {
                if (nm.equals(optNm)) {
                    return option;
                }
            }
        }
        return null;
    }

    public final String description;

    private Map<String, Option> __opts = new LinkedHashMap<>();
}
