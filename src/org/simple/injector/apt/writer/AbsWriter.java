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

package org.simple.injector.apt.writer;

import org.simple.injector.SimpleDagger;
import org.simple.injector.util.AnnotationUtil;
import org.simple.injector.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

/**
 * @author mrsimple
 */
public abstract class AbsWriter implements AdapterWriter {

    ProcessingEnvironment mProcessingEnv;
    Filer mFiler;

    public AbsWriter(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
        mFiler = processingEnv.getFiler();
    }

    @Override
    public void generate(Map<String, List<VariableElement>> typeMap) {
        Iterator<Entry<String, List<VariableElement>>> iterator = typeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, List<VariableElement>> entry = iterator.next();
            List<VariableElement> cacheElements = entry.getValue();
            if (cacheElements == null || cacheElements.size() == 0) {
                continue;
            }

            // 取第一个元素来构造注入信息
            InjectorInfo info = createInjectorInfo(cacheElements.get(0));
            Writer writer = null;
            JavaFileObject javaFileObject;
            try {
                javaFileObject = mFiler.createSourceFile(info.getClassFullPath());
                writer = javaFileObject.openWriter();
                // 写入package, import, class以及findViews函数等代码段
                generateImport(writer, info);
                // 写入该类中的所有字段到findViews方法中
                for (VariableElement variableElement : entry.getValue()) {
                    writeField(writer, variableElement, info);
                }
                // 写入findViews函数的大括号以及类的大括号
                writeEnd(writer);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeQuitly(writer);
            }

        }
    }

    /**
     * @param element
     * @return
     */
    protected InjectorInfo createInjectorInfo(VariableElement element) {
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        String packageName = AnnotationUtil.getPackageName(mProcessingEnv, typeElement);
        String className = typeElement.getSimpleName().toString();
        return new InjectorInfo(packageName, className);
    }

    /**
     * @param writer
     * @param info
     * @throws IOException
     */
    protected abstract void generateImport(Writer writer, InjectorInfo info)
            throws IOException;

    /**
     * @param writer
     * @param element
     * @param info
     * @throws IOException
     */
    protected abstract void writeField(Writer writer, VariableElement element, InjectorInfo info)
            throws IOException;

    /**
     * @param writer
     * @throws IOException
     */
    protected abstract void writeEnd(Writer writer) throws IOException;

    /**
     * 注解相关的信息实体类
     * 
     * @author mrsimple
     */
    public static class InjectorInfo {
        /**
         * 被注解的类的包名
         */
        public String packageName;
        /**
         * 被注解的类的类名
         */
        public String classlName;
        /**
         * 要创建的InjectAdapter类的完整路径,新类的名字为被注解的类名 + "$InjectAdapter", 与被注解的类在同一个包下
         */
        public String newClassName;

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
