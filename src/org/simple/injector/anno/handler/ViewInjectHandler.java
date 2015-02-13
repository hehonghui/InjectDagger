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

package org.simple.injector.anno.handler;

import org.simple.injector.anno.ViewInjector;
import org.simple.injector.util.AnnotationUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * @author mrsimple
 */
public class ViewInjectHandler implements AnnotationHandler {

    ProcessingEnvironment mProcessingEnv;

    @Override
    public void attachProcessingEnv(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    @Override
    public Map<String, List<VariableElement>> handleAnnotation(RoundEnvironment roundEnv) {
        Map<String, List<VariableElement>> annotationMap = new HashMap<String, List<VariableElement>>();
        // 获取使用ViewInjector注解的所有元素
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(ViewInjector.class);
        for (Element element : elementSet) {
            // 注解的字段
            VariableElement varElement = (VariableElement) element;
            // 类型的完整路径名,比如某个Activity的完整路径
            String className = getParentClassName(varElement);
            // 获取这个类型的所有注解,例如某个Activity中的所有View的注解对象
            List<VariableElement> cacheElements = annotationMap.get(className);
            if (cacheElements == null) {
                cacheElements = new LinkedList<VariableElement>();
            }
            // 将元素添加到该类型对应的字段列表中
            cacheElements.add(varElement);
            // 以类的路径为key,字段列表为value,存入map.
            // 这里是将所在字段按所属的类型进行分类
            annotationMap.put(className, cacheElements);
        }

        return annotationMap;
    }

    /**
     * 获取某个字段所属的类的完整路径
     * 
     * @param varElement 字段元素
     * @return
     */
    private String getParentClassName(VariableElement varElement) {
        // 获取该元素所在的类型,例如某个View是某个Activity的字段,这里就是获取这个Activity的类型
        TypeElement typeElement = (TypeElement) varElement.getEnclosingElement();
        // 获取typeElement的包名
        String packageName = AnnotationUtil.getPackageName(mProcessingEnv, typeElement);
        // 类型的完整路径名,比如某个Activity的完整路径
        return packageName + "." + typeElement.getSimpleName().toString();
    }
}
