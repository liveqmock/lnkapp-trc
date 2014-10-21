
package org.fbi.linking.codec.dataformat;

import org.fbi.linking.codec.dataformat.annotation.FixedLengthTextMessage;
import org.fbi.linking.codec.dataformat.annotation.Link;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.trc.tqc.internal.AppActivator;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * User: zhanrui
 * Date: 13-9-8
 */
public class AnnotationModelLoader {
    private Set<Class<? extends Annotation>> annotations;

    public AnnotationModelLoader() {
        annotations = new LinkedHashSet<Class<? extends Annotation>>();
        annotations.add(SeperatedTextMessage.class);
        annotations.add(FixedLengthTextMessage.class);
        annotations.add(Link.class);
    }

    public Set<Class<?>> loadModels(String... packageNames) throws Exception {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        for (String pkg : packageNames) {
            find(pkg, classes);
        }
        return classes;
    }

    private void find(String packageName, Set<Class<?>> classes) throws Exception {
        packageName = packageName.replace('.', '/');

        Enumeration<URL> urls = this.getClass().getClassLoader().getResources(packageName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            String urlPath = url.getFile();
            urlPath = URLDecoder.decode(urlPath, "UTF-8");

            if (url.toString().startsWith("bundle:")) {
                BundleContext bundleContext = AppActivator.getBundleContext();

                Enumeration<URL> classFileEntries = bundleContext.getBundle().findEntries(urlPath, "*.class", true);
                if (classFileEntries == null || !classFileEntries.hasMoreElements()) {
                    throw new RuntimeException(String.format("Bundle[%s] not exist Java class @", bundleContext.getBundle().getSymbolicName()));
                }

                while (classFileEntries.hasMoreElements()) {
                    String bundleOneClassName = classFileEntries.nextElement().getPath();
                    bundleOneClassName = bundleOneClassName.replace("/", ".").substring(0, bundleOneClassName.lastIndexOf("."));
                    while (bundleOneClassName.startsWith(".")) {
                        bundleOneClassName = bundleOneClassName.substring(1);
                    }
                    Class<?> bundleOneClass = null;
                    try {
                        bundleOneClass = bundleContext.getBundle().loadClass(bundleOneClassName);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (matches(bundleOneClass)) {
                        classes.add(bundleOneClass);
                    }
                }
                continue;
            }

            if (urlPath.startsWith("file:")) {
                try {
                    urlPath = new URI(url.getFile()).getPath();
                } catch (URISyntaxException e) {
                    //TODO
                }

                if (urlPath.startsWith("file:")) {
                    urlPath = urlPath.substring(5);
                }
            }

            //TODO  JAR的情况
            File file = new File(urlPath);
            if (file.isDirectory()) {
                loadImplementationsInDirectory(packageName, file, classes);
            } else {
                throw new RuntimeException("Urlpath is not supported.");
            }
        }
    }

    private void loadImplementationsInDirectory(String parent, File location, Set<Class<?>> classes) throws ClassNotFoundException {
        File[] files = location.listFiles();
        StringBuilder builder;

        for (File file : files) {
            builder = new StringBuilder(100);
            String name = file.getName();
            if (name != null) {
                name = name.trim();
                builder.append(parent).append("/").append(name);
                String packageOrClass = parent == null ? name : builder.toString();

                if (file.isDirectory()) {
                    loadImplementationsInDirectory(packageOrClass, file, classes);
                } else if (name.endsWith(".class")) {
                    String externalName = packageOrClass.substring(0, packageOrClass.indexOf('.')).replace('/', '.');
                    Class<?> type = this.getClass().getClassLoader().loadClass(externalName);
                    //判断是否符合报文类注解
                    if (matches(type)) {
                        classes.add(type);
                    }
                }
            }
        }
    }


    private List<String> doLoadJarClassEntries(InputStream stream, String urlPath) {
        List<String> entries = new ArrayList<String>();

        JarInputStream jarStream = null;
        try {
            jarStream = new JarInputStream(stream);

            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name != null) {
                    name = name.trim();
                    if (!entry.isDirectory() && name.endsWith(".class")) {
                        entries.add(name);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Cannot search jar file '" + urlPath + " IOException: " + ioe.getMessage(), ioe);
        } finally {
            try {
                if (jarStream != null) {
                    jarStream.close();
                }
            } catch (IOException e) {
                //
            }
        }

        return entries;
    }
    private List<String> doLoadBundleClassEntries(InputStream stream, String urlPath) {
        List<String> entries = new ArrayList<String>();

        JarInputStream jarStream = null;
        try {
            jarStream = new JarInputStream(stream);

            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name != null) {
                    name = name.trim();
                    if (!entry.isDirectory() && name.endsWith(".class")) {
                        entries.add(name);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Cannot search jar file '" + urlPath + " IOException: " + ioe.getMessage(), ioe);
        } finally {
            try {
                if (jarStream != null) {
                    jarStream.close();
                }
            } catch (IOException e) {
                //
            }
        }

        return entries;
    }

    private void doLoadImplementationsInJar(String parent, List<String> entries, Set<Class<?>> classes) throws ClassNotFoundException {
        for (String entry : entries) {
            if (entry.startsWith(parent)) {
                //String externalName = packageOrClass.substring(0, packageOrClass.indexOf('.')).replace('/', '.');
                String externalName = entry.substring(0, entry.indexOf('.')).replace('/', '.');
                System.out.println("=====---===" + externalName);
                Class<?> type = this.getClass().getClassLoader().loadClass(externalName);
                //判断是否符合报文类注解
                if (matches(type)) {
                    classes.add(type);
                }
            }
        }
    }

    private void doLoadImplementationsInBundle(String parent, List<String> entries, Set<Class<?>> classes) throws ClassNotFoundException {
        for (String entry : entries) {
            if (entry.startsWith(parent)) {
                //String externalName = packageOrClass.substring(0, packageOrClass.indexOf('.')).replace('/', '.');
                String externalName = entry.substring(0, entry.indexOf('.')).replace('/', '.');
                System.out.println("=====---===" + externalName);
                Class<?> type = this.getClass().getClassLoader().loadClass(externalName);
                //判断是否符合报文类注解
                if (matches(type)) {
                    classes.add(type);
                }
            }
        }
    }


    private boolean matches(Class<?> type) {
        if (type == null) {
            return false;
        }
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotation(type, annotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnnotation(AnnotatedElement elem, Class<? extends Annotation> annotationType) {
        if (elem.isAnnotationPresent(annotationType)) {
            return true;
        }
        for (Annotation a : elem.getAnnotations()) {
            for (Annotation meta : a.annotationType().getAnnotations()) {
                if (meta.annotationType().getName().equals(annotationType.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
