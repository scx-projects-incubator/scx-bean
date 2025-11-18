package cool.scx.bean;

import cool.scx.bean.dependency_resolver.BeanDependencyResolver;
import cool.scx.bean.provider.BeanProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// BeanFactoryImpl
///
/// @author scx567888
/// @version 0.0.1
public final class BeanFactoryImpl implements BeanFactory {

    private final Map<String, BeanProvider> beanProviders;
    private final List<BeanDependencyResolver> beanDependencyResolvers;

    public BeanFactoryImpl() {
        this.beanProviders = new ConcurrentHashMap<>();
        this.beanDependencyResolvers = new ArrayList<>();
    }

    @Override
    public Map<String, BeanProvider> beanProviders() {
        return beanProviders;
    }

    @Override
    public List<BeanDependencyResolver> beanDependencyResolvers() {
        return beanDependencyResolvers;
    }

}
