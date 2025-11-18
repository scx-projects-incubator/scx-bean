package cool.scx.bean;

import cool.scx.bean.dependency_resolver.BeanDependencyResolver;
import cool.scx.bean.exception.*;
import cool.scx.bean.provider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/// BeanFactory
///
/// @author scx567888
/// @version 0.0.1
public interface BeanFactory {

    /// 获取所有 Bean 提供器 (允许外部修改)
    Map<String, BeanProvider> beanProviders();

    /// 获取所有 Bean 依赖解析器 (允许外部修改)
    List<BeanDependencyResolver> beanDependencyResolvers();

    /// 根据 名称 获取 Bean
    default Object getBean(String name, BeanResolutionContext beanResolutionContext) throws NoSuchBeanException, BeanCreationException {
        var beanProvider = beanProviders().get(name);
        if (beanProvider == null) {
            throw new NoSuchBeanException("未找到任何符合名称的 bean, name = [" + name + "]");
        }
        return beanProvider.getBean(this, beanResolutionContext);
    }

    /// 根据 类型 获取 Bean
    @SuppressWarnings("unchecked")
    default <T> T getBean(Class<T> type, BeanResolutionContext beanResolutionContext) throws NoSuchBeanException, NoUniqueBeanException, BeanCreationException {
        var list = new ArrayList<BeanProvider>();
        for (var beanProvider : beanProviders().values()) {
            if (type.isAssignableFrom(beanProvider.beanClass())) {
                list.add(beanProvider);
            }
        }
        var size = list.size();
        if (size == 0) {
            throw new NoSuchBeanException("未找到任何符合类型的 bean, class = [" + type.getName() + "]");
        }
        if (size > 1) {
            throw new NoUniqueBeanException("找到多个符合类型的 bean, class = [" + type.getName() + "], 已找到 = [" + list.stream().map(c -> c.beanClass().getName()).collect(Collectors.joining(", ")) + "]");
        }
        var beanProvider = list.get(0);
        return (T) beanProvider.getBean(this, beanResolutionContext);
    }

    /// 根据 名称和类型 获取 Bean
    @SuppressWarnings("unchecked")
    default <T> T getBean(String name, Class<T> type, BeanResolutionContext beanResolutionContext) throws NoSuchBeanException, BeanCreationException {
        var beanProvider = beanProviders().get(name);
        if (beanProvider == null) {
            throw new NoSuchBeanException("未找到任何符合名称的 bean, name = [" + name + "]");
        }
        if (!type.isAssignableFrom(beanProvider.beanClass())) {
            throw new NoSuchBeanException("未找到任何符合名称的 bean, name = [" + name + "], class = [" + type.getName() + "]");
        }
        return (T) beanProvider.getBean(this, beanResolutionContext);
    }

    /// 根据 名称 获取 Bean
    default Object getBean(String name) throws NoSuchBeanException, BeanCreationException {
        return getBean(name, new BeanResolutionContext());
    }

    /// 根据 类型 获取 Bean
    default <T> T getBean(Class<T> type) throws NoSuchBeanException, NoUniqueBeanException, BeanCreationException {
        return getBean(type, new BeanResolutionContext());
    }

    /// 根据 名称和类型 获取 Bean
    default <T> T getBean(String name, Class<T> type) throws NoSuchBeanException, BeanCreationException {
        return getBean(name, type, new BeanResolutionContext());
    }

    /// 获取所有 Bean 的名字
    default String[] getBeanNames() {
        return beanProviders().keySet().toArray(String[]::new);
    }

    /// 根据 Class 注册一个 Bean
    ///
    /// @param singleton 是否单例
    /// @param injecting 是否注入字段
    default void registerBeanClass(String name, Class<?> beanClass, boolean singleton, boolean injecting) throws IllegalBeanClassException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateBeanNameException {
        BeanProvider beanProvider = new AnnotationConfigBeanProvider(beanClass);
        if (singleton) {
            beanProvider = new SingletonBeanProvider(beanProvider);
        }
        if (injecting) {
            beanProvider = new InjectingBeanProvider(beanProvider);
        }
        // 注册, 不允许重复
        var oldValue = beanProviders().putIfAbsent(name, beanProvider);
        if (oldValue != null) {
            throw new DuplicateBeanNameException("重复的 bean name, name = [" + name + "]");
        }
    }

    /// 根据 Class 注册一个 Bean (默认 注入字段)
    ///
    /// @param singleton 是否单例
    default void registerBeanClass(String name, Class<?> beanClass, boolean singleton) throws IllegalBeanClassException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateBeanNameException {
        registerBeanClass(name, beanClass, singleton, true);
    }

    /// 根据 Class 注册一个 Bean (默认 注入字段, 单例模式)
    default void registerBeanClass(String name, Class<?> beanClass) throws IllegalBeanClassException, NoSuchConstructorException, NoUniqueConstructorException, DuplicateBeanNameException {
        registerBeanClass(name, beanClass, true, true);
    }

    /// 注册一个单例的 Bean
    ///
    /// @param injecting 是否注入字段
    default void registerBean(String name, Object bean, boolean injecting) throws DuplicateBeanNameException {
        BeanProvider beanProvider = new InstanceBeanProvider(bean);
        if (injecting) {
            beanProvider = new InjectingBeanProvider(beanProvider);
        }
        // 注册, 不允许重复
        var oldValue = beanProviders().putIfAbsent(name, beanProvider);
        if (oldValue != null) {
            throw new DuplicateBeanNameException("重复的 bean name, name = [" + name + "]");
        }
    }

    /// 注册一个单例的 Bean (默认 不注入字段)
    default void registerBean(String name, Object bean) throws DuplicateBeanNameException {
        registerBean(name, bean, false);
    }

    /// 初始化所有 Bean
    default void initializeBeans() throws BeanCreationException {
        for (var beanProvider : beanProviders().values()) {
            beanProvider.getBean(this, new BeanResolutionContext());
        }
    }

}
