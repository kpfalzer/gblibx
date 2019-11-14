package gblibx;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.impl.type.FileArgumentType;
import net.sourceforge.argparse4j.inf.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static gblibx.Util.*;
import static java.lang.Character.isDigit;
import static java.util.Objects.isNull;

public class ArgParse {
    public ArgParse(String progNm, String description) {
        __parser = ArgumentParsers
                .newFor(progNm)
                .singleMetavar(true)
                .build()
                .description(description);
    }

    public ArgParse addToGroup(String descrip, Collection<Arg> args) {
        return addToGroup(descrip, args.stream());
    }

    public ArgParse addToGroup(String descrip, Stream<Arg> args) {
        ArgumentGroup grp = __parser.addArgumentGroup(descrip);
        args.forEach((arg) -> arg.addTo(grp));
        return this;
    }

    public ArgParse addTo(Arg[] args) {
        return addTo(Arrays.stream(args));
    }

    public ArgParse addTo(Stream<Arg> args) {
        args.forEach((arg) -> arg.addTo(__parser));
        return this;
    }

    public boolean parse(String[] argv) {
        try {
            __args = __parser.parseArgs(argv);
        } catch (ArgumentParserException e) {
            __parser.handleError(e);
            return false;
        }
        return true;
    }

    public Namespace values() {
        return __args;
    }

    private final ArgumentParser __parser;
    private Namespace __args;

    private static class RegexpArgAction implements ArgumentAction {

        @Override
        public void run(ArgumentParser argumentParser, Argument argument, Map<String, Object> attrs, String s, Object o) throws ArgumentParserException {
            ArrayList<String> items = downcast(o);
            ArrayList<Pattern> values = new ArrayList<>();
            for (String item : items) {
                try {
                    final Pattern p = Pattern.compile(item);
                    values.add(p);
                } catch (PatternSyntaxException ex) {
                    final String detail = "'" + ex.getPattern() + "': " + ex.getDescription();
                    throw new ArgumentParserException(detail, argumentParser, argument);
                }
            }
            attrs.put(argument.getDest(), values);
        }

        @Override
        public void onAttach(Argument argument) {
            ;//do nothing
        }

        @Override
        public boolean consumeArgument() {
            return true;
        }
    }

    public static Arg argWithDefault(String optnm, String metavar, String helpFmt, String opt, String dflt) {
        return new Arg(optnm, metavar, String.format(helpFmt, dflt), opt, dflt, true);
    }

    public static class Arg {

        /**
         * Specify not required option with String args.
         *
         * @param optnm   '|' separated option names.
         * @param metavar argument name.
         * @param help    help string.
         * @param nrep    ?|+|*|n number of args.
         */
        public Arg(String optnm, String metavar, String help, char nrep) {
            this(optnm, metavar, help, "S_" + nrep);
        }

        /**
         * Specify option with type argument.
         *
         * @param optnm   '|' separated option names.
         * @param metavar argument name.
         * @param help    help string.
         * @param opt     (I|R|W|S|X|B)(!_)(?|+|*|n)
         *                Integer, String, File-Readable, File-Writable, Regexp, Binary.
         *                NOTE: (B)inary above should have '_0' as next 2 args (not checked).
         *                ! indicates required; _ indicates not required arg.
         *                ?+*n is number of args.
         */
        public Arg(String optnm, String metavar, String help, String opt) {
            this(optnm, metavar, help);
            invariant(3 == opt.length());
            __narg = opt.charAt(2);
            __required = ('!' == opt.charAt(1));
            switch (opt.charAt(0)) {
                case 'R':
                    __fileType = Arguments.fileType().verifyIsFile().verifyCanRead();
                    break;
                case 'W':
                    __fileType = Arguments.fileType().verifyCanWrite();
                    break;
                case 'I':
                    __cls = Integer.class;
                    break;
                case 'S':
                    __cls = String.class;
                    break;
                case 'X':
                    //NOTE: there is no java.Regex, so we just put reasonable class here.
                    __cls = PatternSyntaxException.class;
                    break;
                case 'B':
                    __cls = null;
                    __fileType = null;
                    __choices = null;
                    break;
                default:
                    Util.expectNever();
            }
        }

        public Arg(String optnm, String metavar, String help, String opt, String dflt, boolean unused) {
            this(optnm, metavar, help, opt);
            __default = dflt;
            __choices = null;
        }

        /**
         * @param optnm
         * @param metavar
         * @param help
         * @param dflt
         * @param choices
         */
        public Arg(String optnm, String metavar, String help, String dflt, boolean unused, String... choices) {
            this(optnm, metavar, help);
            __choices = (0 < choices.length) ? choices : null;
            __default = dflt;
        }

        private Arg(String optnm, String metavar, String help) {
            __optnm = optnm.split("\\|");
            __metavar = metavar;
            __help = help;
        }

        static {
            // Check we can do char based math...
            invariant('9' > '0');
        }

        private ArgumentContainer addTo(ArgumentContainer parser) {
            Argument arg = parser.addArgument(__optnm);
            arg
                    .help(__help)
                    .required(__required);
            acceptIfNotNull(__metavar, m -> arg.metavar(m));
            if (isNull(__choices)) {
                if (isDigit(__narg)) {
                    int nn = __narg - '0';
                    if (0 < nn)
                        arg.nargs(nn);
                    else
                        arg
                                .action(Arguments.storeTrue())
                                .setDefault(false);
                } else {
                    arg.nargs(Character.toString(__narg));
                }
                if (isNonNull(__cls)) {
                    if (__cls == PatternSyntaxException.class) {
                        arg.action(new RegexpArgAction());
                    } else
                        arg.type(__cls);
                } else if (isNonNull(__fileType)) {
                    arg.type(__fileType);
                }
                if (isNonNull(__default)) {
                    arg.setDefault(__default);
                }
            } else {
                arg.required(false).nargs(1).choices(__choices).setDefault(__default);
            }
            return parser;
        }

        private String[] __optnm;
        private String __metavar;
        private String __help;
        private Class __cls;
        private FileArgumentType __fileType;
        private boolean __required;
        private char __narg;
        private String[] __choices;
        private String __default;
    }
}
