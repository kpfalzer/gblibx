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

import gblibx.Util;
import sun.jvm.hotspot.asm.sparc.SPARCArgument;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static gblibx.Util.Pair;
import static gblibx.Util.invariant;
import static gblibx.Util.isNonNull;
import static gblibx.Util.join;
import static gblibx.Util.toArrayOfT;
import static java.util.Objects.isNull;

/**
 * An option spec and container for arg parsing.
 */
public class Option {
    public Option(String optNm, String argNm, Object dflt,
                  String description, char repeat,
                  Function<Object, Pair<Object, String>> convert) {
        this.optNm = optNm.split("\\|");
        this.description = description;
        this.argNm = argNm;
        this.repeat = repeat;
        this.dflt = dflt;
        this.convert = convert;
        //
        invariant(2 >= this.optNm.length);
        if (1 < this.optNm.length) invariant(this.optNm[0].length() < this.optNm[1].length());
        invariant(0 <= VALID_REPEAT.indexOf(this.repeat));
        if (isNonNull(this.dflt)) invariant(takesArg());
    }

    public Option(String optNm, String argNm, Object dflt,
                  String description, char repeat) {
        this(optNm, argNm, dflt, description, repeat, null);
    }

    /**
     * Binary option.
     *
     * @param optNm       name of option.
     * @param description description.
     */
    public Option(String optNm, String description) {
        this(optNm, null, null, description, '?', null);
    }

    /**
     * Optional ('?') option.
     *
     * @param optNm       name of option.
     * @param argNm       argument name.
     * @param description description.
     */
    public Option(String optNm, String argNm, String description) {
        this(optNm, argNm, null, description, '?', null);
    }

    public boolean hasOpts() {
        return isNonNull(__opts) && (0 < __opts.size());
    }

    public boolean takesArg() {
        return isNonNull(argNm);
    }

    public boolean isBinaryOpt() {
        return isOptional() && !takesArg();
    }

    public Object[] getOpts() {
        if (hasOpts())
            return __opts.toArray(new Object[0]);
        else if (takesArg()) {
            invariant(isNonNull(dflt));
            return new Object[]{dflt};
        }
        else if (isBinaryOpt() && !hasOpts())
            return __FALSE;
        return EMPTY;
    }

    public <T> T[] getOptsAsT() {
        return toArrayOfT(getOpts());
    }

    public <T> T asScalar() {
        T[] vals = getOptsAsT();
        invariant(1 >= vals.length);
        return (1 == vals.length) ? vals[0] : null;
    }

    public Boolean[] getOptsAsBoolean() {
        return this.<Boolean>getOptsAsT();
    }

    public String[] getOptsAsString() {
        return this.<String>getOptsAsT();
    }

    public String getOptAsString() {
        return asScalar();
    }

    public Boolean getOptAsBoolean() {
        return asScalar();
    }

    public Integer getOptAsInteger() {
        return asScalar();
    }

    public boolean isTrue() {
        invariant(isBinaryOpt());
        return asScalar();
    }

    public class BadOption extends RuntimeException {
        public BadOption(String msg) {
            super(prOptNm() + ": " + msg);
        }
    }

    private void __badOption(String msg) {
        throw new BadOption(msg);
    }

    public boolean isOptional() {
        return '?' == repeat;
    }

    public void setBinaryOpt(Boolean val) {
        invariant(isBinaryOpt());
        if (isTrue()) {
            __badOption("option already specified");
        }
    }

    public void setBinaryOpt() {
        setBinaryOpt(Boolean.TRUE);
    }

    public void addOpt(Object opt) {
        if (!takesArg()) {
            __badOption("unexpected argument: " + opt);
        }
        if (isOptional() && hasOpts()) {
            __badOption("< 1 occurrence at: " + opt);
        }
        if (isNonNull(convert)) {
            Pair<Object, String> converted = convert.apply(opt);
            if (isNonNull(converted.v2)) {
                __badOption("invalid argument: " + converted.v2);
            }
            opt = converted.v1;
        }
        __addOpt(opt);
    }

    private void __addOpt(Object opt) {
        if (isNull(__opts)) __opts = new LinkedList<>();
        __opts.add(opt);
    }

    public boolean validate() {
        if ('!' == repeat && !hasOpts()) {
            __badOption("required option");
        }
        return true;
    }

    public boolean isRequiredMet() {
        return (!isRequired() || (isNonNull(__opts) && !__opts.isEmpty()));
    }

    public String optNm() {
        return ((1 == optNm.length) ? optNm[0] : optNm[1]).replaceAll("^\\-+", "");
    }

    public String prOptNm() {
        return join(this.optNm, "|");
    }

    public boolean isRequired() {
        return 0 <= "!+".indexOf(repeat);
    }

    public boolean isRepeated() {
        return 0 <= "+*".indexOf(repeat);
    }

    /**
     * Short usage.
     *
     * @return short usage.
     */
    public String toString() {
        StringBuilder usage = new StringBuilder();
        String optNm = prOptNm();
        if (isBinaryOpt()) {
            usage.append('[').append(optNm).append(']');
        } else {
            if (isRequired()) {
                usage.append(optNm);
                if (takesArg()) usage.append(' ').append(argNm);
                if (isRepeated()) usage.append(' ');
            }
            usage.append("[").append(optNm);
            if (takesArg()) {
                usage.append(' ').append(argNm);
            }
            usage.append("]");
            if (isRepeated()) usage.append("...");
        }
        return usage.toString();
    }

    /**
     * Get prefix "--option argNm" lead to detailed usage.
     * Useful to determine optimal indentation.
     *
     * @return prefix "--option argNm'
     */
    public String getDetailedArgs() {
        StringBuilder buf = new StringBuilder();
        buf.append(prOptNm());
        if (isNonNull(argNm)) {
            buf.append(' ').append(argNm);
        }
        return buf.toString();
    }

    /**
     * Detailed usage.
     *
     * @param justify column where description should start.
     *
     * @return Detailed usage
     */
    public String getDetailedUsage(int justify) {
        StringBuilder buf = new StringBuilder();
        buf.append(getDetailedArgs());
        int padding = justify - buf.length();
        if (padding < 2) padding = 2;
        buf.append(Util.repeat(" ", padding));
        buf.append(description);
        return buf.toString();
    }

    /**
     * Force a new option.
     * @return new Option.
     */
    public static Option forceAddOption(String optNm, Object value) {
        Option opt = new Option(optNm, null);
        opt.__addOpt(value);
        return opt;
    }

    public static final String VALID_REPEAT = "!?*+";
    public static final String[] EMPTY = {};
    private static final Boolean[] __FALSE = {Boolean.FALSE};

    public final String[] optNm;
    public final String description;
    public final String argNm;
    public final char repeat;
    public final Object dflt;
    public final Function<Object, Pair<Object, String>> convert;

    private List<Object> __opts = null;
}
