package org.androidannotations.ebean;

import com.googlecode.androidannotations.AndroidAnnotationProcessor;
import com.googlecode.androidannotations.utils.AAProcessorTestHelper;
import org.androidannotations.AndroidAnnotationProcessor;
import org.androidannotations.utils.AAProcessorTestHelper;
import org.junit.Before;
import org.junit.Test;

public class EBeanTest extends AAProcessorTestHelper {

    @Before
    public void setup() {
        addManifestProcessorParameter(EBeanTest.class);
        addProcessor(AndroidAnnotationProcessor.class);
    }

    @Test
    public void activity_subclass_in_manifest_compiles() {
        assertCompilationSuccessful(compileFiles(SomeActivity.class, SomeImplementation.class));
    }
}

