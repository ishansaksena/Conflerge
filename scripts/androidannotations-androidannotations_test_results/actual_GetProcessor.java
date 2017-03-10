/**
 * Copyright (C) 2010-2012 eBusiness Information, Excilys Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.androidannotations.processing.rest;

import com.googlecode.androidannotations.annotations.rest.Get;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JVar;
import java.lang.annotation.Annotation;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.helper.CanonicalNameConstants;
import org.androidannotations.processing.EBeanHolder;

public class GetProcessor extends GetPostProcessor {

    public GetProcessor(ProcessingEnvironment processingEnv, RestImplementationsHolder restImplementationHolder) {
        super(processingEnv, restImplementationHolder);
    }

    @Override
    public Class<? extends Annotation> getTarget() {
        return Get.class;
    }

    @Override
    public String retrieveUrlSuffix(Element element) {
        Get getAnnotation = element.getAnnotation(Get.class);
        return getAnnotation.value();
    }
}

