import org.junit.internal.TextListener;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * JUnitCore for cardtest.py, with one thing more: a class whose tests are JUnit 5's {@code @ParameterizedTest} over a
 * {@code @CsvSource} is run too, one test for each row (library/FormatLibraryTests is the server module's only one).
 * The server's test classpath has JUnit 4 and the Jupiter annotations but no Jupiter engine, so JUnitCore finds no
 * test in such a class and Maven alone ran it.
 *
 * cardtest.py compiles this beside the tests and names it in place of org.junit.runner.JUnitCore only when a class it
 * is asked to run has such a test; every other class is handed to JUnit 4's own runner, exactly as JUnitCore would.
 *
 * It is no Jupiter engine. It knows rows of plain values, split at the source's delimiter with its quote character
 * honoured, given to parameters that are strings, numbers, booleans or enums. Anything else Jupiter offers (another
 * source of arguments, a text block, a lifecycle method, a plain Jupiter @Test) is reported as a failing test that
 * says so, never passed over in silence.
 */
public class RowsRunner {
    public static void main(String[] args) throws Exception {
        System.out.println("JUnit version " + junit.runner.Version.id());
        List<Class<?>> classes = new ArrayList<>();
        // Found and not yet initialised: a test class's static set-up must run under JUnit, as it does from JUnitCore
        // (AppConfig tells a test run from the server by an org.junit frame on the stack, and LoadedLibraries loads
        // the cards in a static block).
        for (String name : args)
            classes.add(Class.forName(name, false, RowsRunner.class.getClassLoader()));
        RunnerBuilder builder = new RunnerBuilder() {
            private final RunnerBuilder usual = new AllDefaultPossibilitiesBuilder();

            @Override
            public Runner runnerForClass(Class<?> type) throws Throwable {
                if (!hasRows(type))
                    return usual.runnerForClass(type);
                if (!hasJUnit4Tests(type))
                    return new Rows(type);
                // Both kinds in one class: each is run by its own runner.
                return new Suite(type, Arrays.asList(usual.runnerForClass(type), new Rows(type))) {
                };
            }
        };
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        Result result = core.run(Request.runner(new Suite(builder, classes.toArray(new Class<?>[0]))));
        System.exit(result.wasSuccessful() ? 0 : 1);
    }

    static boolean hasRows(Class<?> type) {
        for (Method method : type.getMethods())
            if (method.isAnnotationPresent(ParameterizedTest.class))
                return true;
        return false;
    }

    static boolean hasJUnit4Tests(Class<?> type) {
        for (Method method : type.getMethods())
            if (method.isAnnotationPresent(org.junit.Test.class))
                return true;
        return false;
    }

    /** One row of one method, or something about a method that cannot be run here. */
    private static class Case {
        final Description description;
        final Method method;
        final String row;
        final String problem;

        Case(Description description, Method method, String row, String problem) {
            this.description = description;
            this.method = method;
            this.row = row;
            this.problem = problem;
        }
    }

    static class Rows extends Runner {
        private final Class<?> type;
        private final Description description;
        private final List<Case> cases = new ArrayList<>();

        Rows(Class<?> type) {
            this.type = type;
            this.description = Description.createSuiteDescription(type);
            for (Annotation annotation : type.getAnnotations()) {
                String name = annotation.annotationType().getName();
                if (name.startsWith("org.junit.jupiter.") && !name.equals("org.junit.jupiter.api.DisplayName"))
                    add(type.getSimpleName(), null, null, type.getSimpleName() + " has @" + annotation.annotationType().getSimpleName()
                            + ", which cardtest.py's runner does not know (it runs @ParameterizedTest over @CsvSource and "
                            + "nothing else of JUnit 5): Maven runs this class");
            }
            Method[] methods = type.getMethods();
            Arrays.sort(methods, Comparator.comparing(Method::getName));
            for (Method method : methods) {
                String unknown = null;
                for (Annotation annotation : method.getAnnotations()) {
                    String name = annotation.annotationType().getName();
                    if (name.startsWith("org.junit.jupiter.") && !(annotation instanceof ParameterizedTest) && !(annotation instanceof CsvSource))
                        unknown = "@" + annotation.annotationType().getSimpleName();
                }
                if (unknown != null) {
                    add(method.getName(), method, null, method.getName() + " has " + unknown + ", which cardtest.py's runner does not know "
                            + "(it runs @ParameterizedTest over @CsvSource and nothing else of JUnit 5): Maven runs this test");
                    continue;
                }
                if (!method.isAnnotationPresent(ParameterizedTest.class))
                    continue;
                CsvSource source = method.getAnnotation(CsvSource.class);
                if (source == null || source.value().length == 0) {
                    add(method.getName(), method, null, method.getName() + " takes its arguments from something other than the rows of a "
                            + "@CsvSource, which cardtest.py's runner does not know: Maven runs this test");
                    continue;
                }
                for (String row : source.value())
                    add(method.getName() + "[" + row + "]", method, row, null);
            }
        }

        private void add(String name, Method method, String row, String problem) {
            Description child = Description.createTestDescription(type, name);
            description.addChild(child);
            cases.add(new Case(child, method, row, problem));
        }

        @Override
        public Description getDescription() {
            return description;
        }

        @Override
        public void run(RunNotifier notifier) {
            for (Case one : cases) {
                notifier.fireTestStarted(one.description);
                try {
                    if (one.problem != null)
                        throw new UnsupportedOperationException(one.problem);
                    CsvSource source = one.method.getAnnotation(CsvSource.class);
                    Object[] arguments = arguments(one.method, cells(one.row, source));
                    Object instance = Modifier.isStatic(one.method.getModifiers()) ? null : type.getDeclaredConstructor().newInstance();
                    one.method.invoke(instance, arguments);
                } catch (InvocationTargetException failed) {
                    notifier.fireTestFailure(new Failure(one.description, failed.getCause()));
                } catch (Throwable failed) {
                    notifier.fireTestFailure(new Failure(one.description, failed));
                } finally {
                    notifier.fireTestFinished(one.description);
                }
            }
        }

        /** A row split at the delimiter, a quoted cell kept whole; an unquoted empty cell is null, as in JUnit 5. */
        private static List<String> cells(String row, CsvSource source) {
            String delimiter = !source.delimiterString().isEmpty() ? source.delimiterString()
                    : source.delimiter() != '\0' ? String.valueOf(source.delimiter()) : ",";
            char quote = source.quoteCharacter();
            List<String> out = new ArrayList<>();
            StringBuilder cell = new StringBuilder();
            boolean quoted = false, wasQuoted = false;
            for (int i = 0; i <= row.length(); i++) {
                boolean atEnd = i == row.length();
                if (!atEnd && row.charAt(i) == quote) {
                    quoted = !quoted;
                    wasQuoted = true;
                } else if (atEnd || (!quoted && row.startsWith(delimiter, i))) {
                    String text = wasQuoted ? cell.toString() : cell.toString().trim();
                    out.add(text.isEmpty() && !wasQuoted ? null : text);
                    cell.setLength(0);
                    wasQuoted = false;
                    if (!atEnd)
                        i += delimiter.length() - 1;
                } else {
                    cell.append(row.charAt(i));
                }
            }
            return out;
        }

        private static Object[] arguments(Method method, List<String> cells) {
            Class<?>[] wanted = method.getParameterTypes();
            if (wanted.length != cells.size())
                throw new IllegalArgumentException(method.getName() + " takes " + wanted.length + " arguments and the row has "
                        + cells.size() + ": " + cells);
            Object[] out = new Object[wanted.length];
            for (int i = 0; i < wanted.length; i++)
                out[i] = converted(cells.get(i), wanted[i], method);
            return out;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Object converted(String cell, Class<?> wanted, Method method) {
            if (cell == null) {
                if (wanted.isPrimitive())
                    throw new IllegalArgumentException(method.getName() + ": an empty cell for a parameter of type " + wanted.getName());
                return null;
            }
            if (wanted == String.class || wanted == Object.class || wanted == CharSequence.class)
                return cell;
            if (wanted == int.class || wanted == Integer.class)
                return Integer.valueOf(cell);
            if (wanted == long.class || wanted == Long.class)
                return Long.valueOf(cell);
            if (wanted == double.class || wanted == Double.class)
                return Double.valueOf(cell);
            if (wanted == float.class || wanted == Float.class)
                return Float.valueOf(cell);
            if (wanted == short.class || wanted == Short.class)
                return Short.valueOf(cell);
            if (wanted == byte.class || wanted == Byte.class)
                return Byte.valueOf(cell);
            if (wanted == boolean.class || wanted == Boolean.class) {
                if (!cell.equalsIgnoreCase("true") && !cell.equalsIgnoreCase("false"))
                    throw new IllegalArgumentException(method.getName() + ": '" + cell + "' is neither true nor false");
                return Boolean.valueOf(cell);
            }
            if (wanted == char.class || wanted == Character.class) {
                if (cell.length() != 1)
                    throw new IllegalArgumentException(method.getName() + ": '" + cell + "' is not one character");
                return cell.charAt(0);
            }
            if (wanted.isEnum())
                return Enum.valueOf((Class<? extends Enum>) wanted, cell);
            throw new UnsupportedOperationException(method.getName() + " has a parameter of type " + wanted.getName()
                    + ", which cardtest.py's runner cannot make from a cell: Maven runs this test");
        }
    }
}
