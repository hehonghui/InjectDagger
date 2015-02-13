/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.simple.injector.apt;

import static javax.tools.Diagnostic.Kind.ERROR;

import org.simple.injector.SimpleDagger;
import org.simple.injector.ViewInjector;
import org.simple.injector.apt.writer.DefaultJavaFileWriter;
import org.simple.injector.apt.writer.AdapterWriter;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@SupportedAnnotationTypes("org.simple.injector.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ViewInjectorProcessor extends AbstractProcessor {

    final Map<String, List<VariableElement>> map = new HashMap<String, List<VariableElement>>();

    AdapterWriter mWriter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mWriter = new DefaultJavaFileWriter(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(ViewInjector.class);
        for (Element element : elementSet) {
            // 注解的字段
            VariableElement varElement = (VariableElement) element;
            // 类型
            TypeElement typeElement = (TypeElement) varElement.getEnclosingElement();
            // 类型的完整路径名,比如某个Activity的完整路径
            String className = getPackageName(typeElement) + "."
                    + typeElement.getSimpleName().toString();
            List<VariableElement> cacheElements = map.get(className);
            if (cacheElements == null) {
                cacheElements = new LinkedList<VariableElement>();
            }

            cacheElements.add(varElement);
            map.put(className, cacheElements);
        }

        mWriter.generate(map);
        return true;
    }

    private String getPackageName(Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    /**
     * @param element
     * @param message
     * @param args
     */
    protected void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    public static class InjectorInfo {

        public String packageName;
        public String newClassName;
        public String classlName;

        public InjectorInfo(String packageName, String classlName) {
            this.packageName = packageName;
            newClassName = classlName + SimpleDagger.SUFFIX;
            this.classlName = classlName;
        }

        public String getClassFullPath() {
            return packageName + File.separator + newClassName;
        }

    }
}
