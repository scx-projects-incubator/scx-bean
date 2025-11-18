package cool.scx.bean.provider;

import cool.scx.bean.BeanFactory;
import cool.scx.bean.BeanResolutionContext;

/// 根据一个 已经存在的 Bean 创建
///
/// @author scx567888
/// @version 0.0.1
public record InstanceBeanProvider(Object bean) implements BeanProvider {

    @Override
    public Object getBean(BeanFactory beanFactory, BeanResolutionContext beanResolutionContext) {
        return bean;
    }

    @Override
    public Class<?> beanClass() {
        return bean.getClass();
    }

    @Override
    public boolean singleton() {
        return true;
    }

}
