/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2006 - 2020 kpfalzer.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

public class Parser {
    public Parser(String progName, String description) {
        this.progName = progName;
        __groups.add(new Group(description));
    }

    public static class Error extends RuntimeException {
        public Error(String msg) {
            super(msg);
        }

        public Error(Option.BadOption bad) {
            super(bad.getMessage());
        }
    }

    private static void __error(String message) {
        throw new Error(message);
    }

    private static void __error(Option.BadOption bad) {
        throw new Error(bad);
    }

    private static final String[] __HELP = {"-h", "--help"};

    private void __doHelp() {
        System.err.println(getDetailedUsage());
        System.exit(1);
    }

    private void __doShortUsage() {
        System.err.println(getShortUsage());
        System.exit(1);
    }

    public boolean parse(String[] argv, boolean needAtLeastOneArg, Consumer<Error> handler) {
        boolean ok = false;
        try {
            ok = parse(argv, needAtLeastOneArg);
        } catch (Error e) {
            handler.accept(e);
        }
        return ok;
    }

    public boolean parse(String[] argv) {
        return parse(argv, true);
    }

    public boolean parse(String[] argv, Consumer<Error> handler) {
        return parse(argv, true, handler);
    }

    public boolean parse(String[] argv, boolean needAtLeastOneArg) {
        if (needAtLeastOneArg && (1 > argv.length)) __doShortUsage();
        __args = new Vector<>(Arrays.asList(argv));
        while (!__args.isEmpty()) {
            String arg = __args.firstElement();
            if (arg.startsWith("-")) {
                if (contains(__HELP, arg)) __doHelp();
                Option option = null;
                for (Group g : __groups) {
                    option = g.find(arg);
                    if (isNonNull(option)) break;
                }
                if (isNull(option)) {
                    __error(arg + ": invalid option");
                }
                //replace option w/ one we will track
                String trackOptNm = option.optNm();
                if (!hasKey(trackOptNm)) __didOpts.put(trackOptNm, option);
                option = __didOpts.get(trackOptNm);
                invariant(isNonNull(option));
                __args.remove(0);
                if (option.takesArg()) {
                    if (__args.isEmpty()) __error(arg + ": takes 1 argument");
                    String optArg = __args.remove(0);
                    option.addOpt(optArg);
                } else {
                    option.setBinaryOpt();
                }
            } else {
                break; //while
            }
        }
        while (!__args.isEmpty()) {
            String opt = __args.remove(0);
            if (opt.startsWith("-")) __error(opt + ": invalid option");
            __posArgs.add(opt);
        }
        __checkAllRequired();
        return true;
    }

    public boolean hasKey(String optNm) {
        return __didOpts.containsKey(optNm);
    }

    public Option getValue(String optNm) {
        return getValue(optNm, false);
    }

    public Option getValue(String optNm, boolean nullOK) {
        if (!nullOK) invariant(hasKey(optNm));
        return __didOpts.get(optNm);
    }

    public <T> T ifhasOption(String optNm, Function<Option, T> onOption) {
        Option opt = getValue(optNm, true);
        return (isNull(opt) || !opt.hasOpts()) ? null : onOption.apply(opt);
    }

    public String getString(String optNm) {
        Option opt = getValue(optNm);
        return (opt.hasOpts()) ? opt.getOptsAsString()[0] : null;
    }

    public Set<String> getDidOptions() {
        return __didOpts.keySet();
    }

    public String getShortUsage() {
        StringBuilder buf = new StringBuilder();
        buf.append("Usage: ").append(progName).append(" [-h|--help]");
        for (Group g : __groups) {
            buf.append(' ').append(g.toString());
        }
        if (isNonNull(__posArgUsage)) buf.append(" ").append(__posArgUsage);
        buf.append("\n");
        return buf.toString();
    }

    public String getDetailedUsage() {
        StringBuilder buf = new StringBuilder(getShortUsage());
        for (Group g : __groups) {
            buf.append("\n").append(g.getDetailedUsage());
        }
        if (isNonNull(__posArgUsage)) buf.append("\n").append(__posArgUsage).append("    ").append(__posArgDescip);
        return buf.toString();
    }

    private void __checkAllRequired() {
        for (Group g : __groups) {
            for (Option option : g.allOptions()) {
                if (!option.isRequiredMet()) {
                    __error(option.prOptNm() + ": option required");
                }
                //add it to done, if not already
                String k = option.optNm();
                if (!hasKey(k)) __didOpts.put(k, option);
            }
        }
    }

    private Vector<String> __args = null;
    private HashMap<String, Option> __didOpts = new HashMap<>();

    public boolean hasPosArgs() {
        return !getPosArgs().isEmpty();
    }

    public ArrayList<String> getPosArgs() {
        return __posArgs;
    }

    public Group getGroup(int ix) {
        return __groups.get(ix);
    }

    public Group getGroup() {
        return getGroup(0);
    }

    public Group add(Option option) {
        return getGroup().add(option);
    }

    public Group add(String optNm, String description) {
        return add(new Option(optNm, description));
    }

    public Group add(String optNm, String argNm, String description) {
        return add(new Option(optNm, argNm, description));
    }

    public Group add(Group group) {
        __groups.add(group);
        return __groups.get(__groups.size() - 1);
    }

    public Parser addPosArgUsage(String arg, String descrip) {
        __posArgUsage = arg;
        __posArgDescip = descrip;
        return this;
    }

    public final String progName;

    private ArrayList<Group> __groups = new ArrayList<>();
    private ArrayList<String> __posArgs = new ArrayList<>();
    private String __posArgUsage, __posArgDescip;
}
