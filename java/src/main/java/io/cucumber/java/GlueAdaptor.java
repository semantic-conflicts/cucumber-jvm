package io.cucumber.java;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class GlueAdaptor {

    private final Lookup lookup;
    private final Glue glue;

    GlueAdaptor(Lookup lookup, Glue glue) {
        this.lookup = lookup;
        this.glue = glue;
    }

    void addDefinition(Method method, Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Creator<? super Annotation, ?> instance = createInstance(annotationType);
        Object definition = instance.create(lookup, method, annotation);

        if (annotationType.getAnnotation(StepDefinitionAnnotation.class) != null) {
            glue.addStepDefinition((StepDefinition) definition);
        } else if (annotationType.equals(Before.class)) {
            //TODO: Hook defintiions need specific types to make this work
            glue.addBeforeHook((HookDefinition) definition);
        } else if (annotationType.equals(After.class)) {
            glue.addAfterHook((HookDefinition) definition);
        } else if (annotationType.equals(BeforeStep.class)) {
            glue.addBeforeStepHook((HookDefinition) definition);
        } else if (annotationType.equals(AfterStep.class)) {
            glue.addAfterStepHook((HookDefinition) definition);
        } else if (annotationType.equals(ParameterType.class)) {
            glue.addParameterType((ParameterTypeDefinition) definition);
        } else if (annotationType.equals(DataTableType.class)) {
            glue.addDataTableType((DataTableTypeDefinition) definition);
        } else if (annotationType.equals(DefaultParameterTransformer.class)) {
            glue.addDefaultParameterTransformer((DefaultParameterTransformerDefinition) definition);
        } else if (annotationType.equals(DefaultDataTableEntryTransformer.class)) {
            glue.addDefaultDataTableEntryTransformer((DefaultDataTableEntryTransformerDefinition) definition);
        } else if (annotationType.equals(DefaultDataTableCellTransformer.class)) {
            glue.addDefaultDataTableCellTransformer((DefaultDataTableCellTransformerDefinition) definition);
        } else if (annotationType.equals(DocStringType.class)) {
            glue.addDocStringType((DocStringTypeDefinition) definition);
        }
    }

    @SuppressWarnings("unchecked")
    private Creator<? super Annotation, ?> createInstance(Class<? extends Annotation> annotationType){
        try {
            // TODO: Scan up for this annotation
            CreatedBy createdBy = annotationType.getAnnotation(CreatedBy.class);
            Class<? extends Creator<?, ?>> creator = createdBy.value();
            return (Creator<? super Annotation, ?>) creator.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    static class DocStringTypeDefinitionCreator implements Creator<DocStringType, DocStringTypeDefinition> {

        @Override
        public DocStringTypeDefinition create(Lookup lookup, Method method, DocStringType annotation) {
            String contentType = annotation.contentType();
            return new JavaDocStringTypeDefinition(contentType, method, lookup);
        }
    }

    static class DefaultDataTableCellTransformerDefinitionCreator implements Creator<DefaultDataTableCellTransformer, DefaultDataTableCellTransformerDefinition> {

        @Override
        public DefaultDataTableCellTransformerDefinition create(Lookup lookup, Method method, DefaultDataTableCellTransformer annotation) {
            String[] emptyPatterns = annotation.replaceWithEmptyString();
            return new JavaDefaultDataTableCellTransformerDefinition(method, lookup, emptyPatterns);
        }
    }

    static class DefaultDataTableEntryTransformerDefinitionCreator implements Creator<DefaultDataTableEntryTransformer, DefaultDataTableEntryTransformerDefinition> {

        @Override
        public DefaultDataTableEntryTransformerDefinition create(Lookup lookup, Method method, DefaultDataTableEntryTransformer annotation) {
            boolean headersToProperties = annotation.headersToProperties();
            String[] replaceWithEmptyString = annotation.replaceWithEmptyString();
            return new JavaDefaultDataTableEntryTransformerDefinition(method, lookup, headersToProperties, replaceWithEmptyString);
        }
    }

    static class DefaultParameterTransformerDefinitionCreator implements Creator<DefaultParameterTransformer, DefaultParameterTransformerDefinition> {

        @Override
        public DefaultParameterTransformerDefinition create(Lookup lookup, Method method, DefaultParameterTransformer annotation) {
            return new JavaDefaultParameterTransformerDefinition(method, lookup);
        }
    }

    static class DataTableTypeDefinitionCreator implements Creator<DataTableType, DataTableTypeDefinition> {

        @Override
        public DataTableTypeDefinition create(Lookup lookup, Method method, DataTableType annotation) {
            return new JavaDataTableTypeDefinition(method, lookup, annotation.replaceWithEmptyString());
        }
    }

    static class ParameterTypeDefinitionCreator implements Creator<ParameterType, ParameterTypeDefinition> {

        @Override
        public ParameterTypeDefinition create(Lookup lookup, Method method, ParameterType annotation) {
            String pattern = annotation.value();
            String name = annotation.name();
            boolean useForSnippets = annotation.useForSnippets();
            boolean preferForRegexMatch = annotation.preferForRegexMatch();
            boolean useRegexpMatchAsStrongTypeHint = annotation.useRegexpMatchAsStrongTypeHint();
            return new JavaParameterTypeDefinition(name, pattern, method, useForSnippets, preferForRegexMatch, useRegexpMatchAsStrongTypeHint, lookup);
        }
    }

    static class AfterStepHookDefinitionCreator implements Creator<AfterStep, HookDefinition> {

        @Override
        public HookDefinition create(Lookup lookup, Method method, AfterStep annotation) {
            String tagExpression = annotation.value();
            return new JavaHookDefinition(method, tagExpression, annotation.order(), lookup);
        }
    }

    static class BeforeStepHookDefinitionCreator implements Creator<BeforeStep, HookDefinition> {

        @Override
        public HookDefinition create(Lookup lookup, Method method, BeforeStep annotation) {
            String tagExpression = annotation.value();
            return new JavaHookDefinition(method, tagExpression, annotation.order(), lookup);
        }
    }

    static class AfterHookDefinitionCreator implements Creator<After, HookDefinition> {

        @Override
        public HookDefinition create(Lookup lookup, Method method, After annotation) {
            String tagExpression = annotation.value();
            return new JavaHookDefinition(method, tagExpression, annotation.order(), lookup);
        }
    }

    static class BeforeHookDefinitionCreator implements Creator<Before, HookDefinition> {

        @Override
        public HookDefinition create(Lookup lookup, Method method, Before annotation) {
            String tagExpression = annotation.value();
            return new JavaHookDefinition(method, tagExpression, annotation.order(), lookup);
        }
    }

    static class StepDefinitionCreator implements Creator<Annotation, StepDefinition> {

        @Override
        public StepDefinition create(Lookup lookup, Method method, Annotation annotation) {
            String expression = expression(annotation);
            return new JavaStepDefinition(method, expression, lookup);
        }
    }

    private static String expression(Annotation annotation) {
        try {
            Method expressionMethod = annotation.getClass().getMethod("value");
            return (String) Invoker.invoke(annotation, expressionMethod);
        } catch (NoSuchMethodException e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }
}
