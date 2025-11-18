package cool.scx.bean.dependency_resolver;

import cool.scx.bean.BeanResolutionContext;
import cool.scx.bean.annotation.Value;
import cool.scx.bean.exception.MissingValueException;
import cool.scx.object.ScxObject;
import cool.scx.object.mapping.NodeMappingException;
import cool.scx.reflect.AnnotatedElementInfo;
import cool.scx.reflect.FieldInfo;
import cool.scx.reflect.ParameterInfo;
import cool.scx.reflect.TypeInfo;

import java.util.Map;

/// 处理 Value 注解
///
/// @author scx567888
/// @version 0.0.1
public final class ValueAnnotationDependencyResolver implements BeanDependencyResolver {

    private final Map<String, Object> map;

    public ValueAnnotationDependencyResolver(Map<String, Object> map) {
        this.map = map;
    }

    public Object resolveValue(AnnotatedElementInfo annotatedElement, TypeInfo javaType) throws NodeMappingException, MissingValueException {
        var annotation = annotatedElement.findAnnotation(Value.class);
        if (annotation == null) {
            return null;
        }
        var rawValue = map.get(annotation.value());
        if (rawValue == null) {
            throw new MissingValueException("未找到 @Value 值 " + annotation.value());
        }
        return ScxObject.convertValue(rawValue, javaType);
    }

    @Override
    public Object resolveConstructorArgument(ParameterInfo parameter, BeanResolutionContext beanResolutionContext) throws NodeMappingException, MissingValueException {
        return resolveValue(parameter, parameter.parameterType());
    }

    @Override
    public Object resolveFieldValue(FieldInfo field, BeanResolutionContext beanResolutionContext) throws NodeMappingException, MissingValueException {
        return resolveValue(field, field.fieldType());
    }

}
