package org.jenkinsci.plugins.mongodb;

import static org.junit.Assert.assertEquals;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Enclosed.class)
public class MongoBuildWrapperValidationTest {

    @RunWith(Parameterized.class)
    public static class CheckPort {

        private String inputValue;
        private Kind expectedKind;
        private String expectedMessage;

        public CheckPort(String inputValue, Kind expectedKind, String expectedMessage) {
            this.inputValue = inputValue;
            this.expectedKind = expectedKind;
            this.expectedMessage = expectedMessage;
        }

        @Test
        public void test() {
            FormValidation actual = MongoBuildWrapper.DescriptorImpl.doCheckPort(inputValue);
            assertEquals(expectedKind, actual.kind);
            assertEquals(expectedMessage, actual.getMessage());
        }

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(
                ok(""),
                ok("0"),
                ok("27017"),
                ok("65535"),
                error("Invalid port number.", "a"),
                error("Invalid port number.", "-1"),
                error("Invalid port number.", "65536"),
                error("Invalid port number.", "100.0")
            );
        }
    }

    public static class CheckDbpath {

        @Rule
        public TemporaryFolder tmpFolder = new TemporaryFolder();

        @Test
        public void empty_directory() {
            File file = tmpFolder.newFolder("foo");
            FormValidation actual = MongoBuildWrapper.DescriptorImpl.doCheckDbpath(file.getAbsolutePath());
            assertEquals(Kind.OK, actual.kind);
        }

        @Test
        public void not_empty_directory() throws IOException {
            File file = tmpFolder.newFolder("foo");
            new File(file, "foo.txt").createNewFile();
            FormValidation actual = MongoBuildWrapper.DescriptorImpl.doCheckDbpath(file.getAbsolutePath());
            assertEquals(Kind.WARNING, actual.kind);
            assertEquals("Not a empty directory. Before running job, the data directory is cleaned.", actual.getMessage());
        }

        @Test
        public void file() throws IOException {
            File file = tmpFolder.newFile("foo");
            FormValidation actual = MongoBuildWrapper.DescriptorImpl.doCheckDbpath(file.getAbsolutePath());
            assertEquals(Kind.ERROR, actual.kind);
            assertEquals("Not a directory.", actual.getMessage());
        }

        @Test
        public void empty_value() throws IOException {
            FormValidation actual = MongoBuildWrapper.DescriptorImpl.doCheckDbpath("");
            assertEquals(Kind.OK, actual.kind);
        }
    }

    private static Object[] ok(Object... params) {
        return toParams(Kind.OK, null, params);
    }

    private static Object[] error(String message, Object... params) {
        return toParams(Kind.ERROR, message, params);
    }

    private static Object[] toParams(Kind kind, String message, Object... params) {
        List<Object> list = new ArrayList<Object>(Arrays.asList(params));
        list.add(kind);
        list.add(message);
        return list.toArray(new Object[params.length + 2]);
    }
}
