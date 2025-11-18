package cool.scx.bean.provider;

import cool.scx.bean.BeanFactory;
import cool.scx.bean.BeanResolutionContext;
import cool.scx.bean.exception.BeanCreationException;

/// 单例的 Bean 提供器
///
/// @author scx567888
/// @version 0.0.1
public final class SingletonBeanProvider implements BeanProvider {

    private final BeanProvider beanProvider;
    private Object bean;

    public SingletonBeanProvider(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
        this.bean = null;
    }

    @Override
    public Object getBean(BeanFactory beanFactory, BeanResolutionContext beanResolutionContext) throws BeanCreationException {
        if (bean == null) {
            bean = beanProvider.getBean(beanFactory, beanResolutionContext);
        }
        return bean;
    }

    @Override
    public Class<?> beanClass() {
        return beanProvider.beanClass();
    }

    @Override
    public boolean singleton() {
        return true;
    }

}
