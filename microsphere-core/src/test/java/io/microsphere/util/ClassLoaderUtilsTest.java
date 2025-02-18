/**
 *
 */
package io.microsphere.util;

import io.microsphere.AbstractTestCase;
import io.microsphere.security.TestSecurityManager;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ClassLoaderUtils} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassLoaderUtils
 * @since 1.0.0
 */
public class ClassLoaderUtilsTest extends AbstractTestCase {

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Test
    public void testFields() throws Exception {


        List<Field> allFields = FieldUtils.getAllFieldsList(ClassLoader.class);

//        echo(ToStringBuilder.reflectionToString(classLoader,ToStringStyle.MULTI_LINE_STYLE));
        Set<ClassLoader> classLoaders = ClassLoaderUtils.getInheritableClassLoaders(classLoader);
        for (ClassLoader classLoader : classLoaders) {
            info(String.format("ClassLoader : %s", classLoader));
            for (Field field : allFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    String message = String.format("Field name : %s , value : %s", field.getName(), ToStringBuilder.reflectionToString(field.get(classLoader), ToStringStyle.NO_CLASS_NAME_STYLE));
                    info(message);
                }
            }
        }

    }


    @Test
    public void testResolve() {
        String resourceName = "META-INF/abc/def";
        String expectedResourceName = "META-INF/abc/def";
        String resolvedResourceName = ClassLoaderUtils.ResourceType.DEFAULT.resolve(resourceName);
        assertEquals(expectedResourceName, resolvedResourceName);

        resourceName = "///////META-INF//abc\\/def";
        resolvedResourceName = ClassLoaderUtils.ResourceType.DEFAULT.resolve(resourceName);
        assertEquals(expectedResourceName, resolvedResourceName);

        resourceName = "java.lang.String.class";

        expectedResourceName = "java/lang/String.class";
        resolvedResourceName = ClassLoaderUtils.ResourceType.CLASS.resolve(resourceName);
        assertEquals(expectedResourceName, resolvedResourceName);

        resourceName = "java.lang";
        expectedResourceName = "java/lang/";
        resolvedResourceName = ClassLoaderUtils.ResourceType.PACKAGE.resolve(resourceName);
        assertEquals(expectedResourceName, resolvedResourceName);

    }

    @Test
    public void testGetClassResource() {
        URL classResourceURL = ClassLoaderUtils.getClassResource(classLoader, ClassLoaderUtilsTest.class);
        assertNotNull(classResourceURL);
        info(classResourceURL);

        classResourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class.getName());
        assertNotNull(classResourceURL);
        info(classResourceURL);
    }

    @Test
    public void testGetResource() {
        URL resourceURL = ClassLoaderUtils.getResource(classLoader, ClassLoaderUtilsTest.class.getName() + CLASS_EXTENSION);
        assertNotNull(resourceURL);
        info(resourceURL);

        resourceURL = ClassLoaderUtils.getResource(classLoader, "///java/lang/CharSequence.class");
        assertNotNull(resourceURL);
        info(resourceURL);

        resourceURL = ClassLoaderUtils.getResource(classLoader, "//META-INF/services/java.lang.CharSequence");
        assertNotNull(resourceURL);
        info(resourceURL);
    }

    @Test
    public void testGetResources() throws IOException {
        Set<URL> resourceURLs = ClassLoaderUtils.getResources(classLoader, ClassLoaderUtilsTest.class.getName() + CLASS_EXTENSION);
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        info(resourceURLs);

        resourceURLs = ClassLoaderUtils.getResources(classLoader, "///java/lang/CharSequence.class");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        info(resourceURLs);

        resourceURLs = ClassLoaderUtils.getResources(classLoader, "//META-INF/services/java.lang.CharSequence");
        assertNotNull(resourceURLs);
        assertEquals(1, resourceURLs.size());
        info(resourceURLs);
    }

    @Test
    public void testClassLoadingMXBean() {
        ClassLoadingMXBean classLoadingMXBean = ClassLoaderUtils.classLoadingMXBean;
        assertEquals(classLoadingMXBean.getTotalLoadedClassCount(), ClassLoaderUtils.getTotalLoadedClassCount());
        assertEquals(classLoadingMXBean.getLoadedClassCount(), ClassLoaderUtils.getLoadedClassCount());
        assertEquals(classLoadingMXBean.getUnloadedClassCount(), ClassLoaderUtils.getUnloadedClassCount());
        assertEquals(classLoadingMXBean.isVerbose(), ClassLoaderUtils.isVerbose());

        ClassLoaderUtils.setVerbose(true);
        assertTrue(ClassLoaderUtils.isVerbose());
    }

    @Test
    public void testGetInheritableClassLoaders() {
        Set<ClassLoader> classLoaders = ClassLoaderUtils.getInheritableClassLoaders(classLoader);
        assertNotNull(classLoaders);
        assertTrue(classLoaders.size() > 1);
        info(classLoaders);
    }

    @Test
    public void testGetLoadedClasses() {
        Set<Class<?>> classesSet = ClassLoaderUtils.getLoadedClasses(classLoader);
        assertNotNull(classesSet);
        assertFalse(classesSet.isEmpty());


        classesSet = ClassLoaderUtils.getLoadedClasses(ClassLoader.getSystemClassLoader());
        assertNotNull(classesSet);
        assertFalse(classesSet.isEmpty());
        info(classesSet);
    }

    @Test
    public void testGetAllLoadedClasses() {
        Set<Class<?>> classesSet = ClassLoaderUtils.getAllLoadedClasses(classLoader);
        assertNotNull(classesSet);
        assertFalse(classesSet.isEmpty());


        classesSet = ClassLoaderUtils.getAllLoadedClasses(ClassLoader.getSystemClassLoader());
        assertNotNull(classesSet);
        assertFalse(classesSet.isEmpty());
        info(classesSet);
    }

    @Test
    public void testGetAllLoadedClassesMap() {
        Map<ClassLoader, Set<Class<?>>> allLoadedClassesMap = ClassLoaderUtils.getAllLoadedClassesMap(classLoader);
        assertNotNull(allLoadedClassesMap);
        assertFalse(allLoadedClassesMap.isEmpty());
    }


    @Test
    public void testFindLoadedClass() {

        Class<?> type = null;
        for (Class<?> class_ : ClassLoaderUtils.getAllLoadedClasses(classLoader)) {
            type = ClassLoaderUtils.findLoadedClass(classLoader, class_.getName());
            assertEquals(class_, type);
        }

        type = ClassLoaderUtils.findLoadedClass(classLoader, String.class.getName());
        assertEquals(String.class, type);

        type = ClassLoaderUtils.findLoadedClass(classLoader, Double.class.getName());
        assertEquals(Double.class, type);
    }

    @Test
    public void testIsLoadedClass() {
        assertTrue(ClassLoaderUtils.isLoadedClass(classLoader, String.class));
        assertTrue(ClassLoaderUtils.isLoadedClass(classLoader, Double.class));
        assertTrue(ClassLoaderUtils.isLoadedClass(classLoader, Double.class.getName()));
    }


    @Test
    public void testFindLoadedClassesInClassPath() {
        Double d = null;
        Set<Class<?>> allLoadedClasses = ClassLoaderUtils.findLoadedClassesInClassPath(classLoader);

        Set<Class<?>> classesSet = ClassLoaderUtils.getAllLoadedClasses(classLoader);

        Set<Class<?>> remainingClasses = new LinkedHashSet<>(allLoadedClasses);

        remainingClasses.addAll(classesSet);

        Set<Class<?>> sortedClasses = new TreeSet(new ClassComparator());
        sortedClasses.addAll(remainingClasses);

        info(sortedClasses);

        int loadedClassesSize = allLoadedClasses.size() + classesSet.size();

        int loadedClassCount = ClassLoaderUtils.getLoadedClassCount();

        info(loadedClassesSize);
        info(loadedClassCount);
    }

    @Test
    public void testGetCount() {
        long count = ClassLoaderUtils.getTotalLoadedClassCount();
        assertTrue(count > 0);

        count = ClassLoaderUtils.getLoadedClassCount();
        assertTrue(count > 0);

        count = ClassLoaderUtils.getUnloadedClassCount();
        assertTrue(count > -1);
    }

    @Test
    public void testFindLoadedClassesInClassPaths() {
        Set<Class<?>> allLoadedClasses = ClassLoaderUtils.findLoadedClassesInClassPaths(classLoader, ClassPathUtils.getClassPaths());
        assertFalse(allLoadedClasses.isEmpty());
    }

    @Test
    public void testOfSet() {
        Set<String> set = of();
        assertEquals(emptySet(), set);

        set = of(((String[]) null));
        assertEquals(emptySet(), set);

        set = of("A", "B", "C");
        Set<String> expectedSet = new LinkedHashSet<>();
        expectedSet.add("A");
        expectedSet.add("B");
        expectedSet.add("C");
        assertEquals(expectedSet, set);
    }

    @Test
    public void testGetClassLoader() {
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        assertEquals(classLoader, ClassLoaderUtils.getDefaultClassLoader());

        currentThread.setContextClassLoader(null);
        assertEquals(ClassLoaderUtils.class.getClassLoader(), ClassLoaderUtils.getDefaultClassLoader());

        currentThread.setContextClassLoader(ClassLoader.getSystemClassLoader().getParent());
        TestSecurityManager.denyRuntimePermission("getClassLoader", () -> {
            new Runnable() {
                @Override
                public void run() {
                    assertEquals(ClassLoaderUtils.class.getClassLoader(), ClassLoaderUtils.getDefaultClassLoader());
                }
            }.run();
        });

    }


    private static class ClassComparator implements Comparator<Class<?>> {

        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            String cn1 = o1.getName();
            String cn2 = o2.getName();
            return cn1.compareTo(cn2);
        }
    }

}
