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

import org.simple.injector.anno.handler.AnnotationHandler;
import org.simple.injector.anno.handler.ViewInjectHandler;
import org.simple.injector.apt.writer.AdapterWriter;
import org.simple.injector.apt.writer.DefaultJavaFileWriter;

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

/**
 * @author mrsimple
 */
@SupportedAnnotationTypes("org.simple.injector.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ViewInjectorProcessor extends AbstractProcessor {

    /**
     * 所有注解处理器的列表
     */
    List<AnnotationHandler> mHandlers = new LinkedList<AnnotationHandler>();
    /**
     * 类型与字段的关联表,用于在写入Java文件时按类型来写不同的文件和字段
     */
    final Map<String, List<VariableElement>> map = new HashMap<String, List<VariableElement>>();
    /**
     * 
     */
    AdapterWriter mWriter;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        registerHandlers();
        mWriter = new DefaultJavaFileWriter(processingEnv);
    }

    /**
     * 
     */
    private void registerHandlers() {
        mHandlers.add(new ViewInjectHandler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (AnnotationHandler handler : mHandlers) {
            // 关联ProcessingEnvironment
            handler.attachProcessingEnv(processingEnv);
            // 解析注解相关的信息
            map.putAll(handler.handleAnnotation(roundEnv));
        }
        // 将解析到的数据写入到具体的类型中
        mWriter.generate(map);
        return true;
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
}
