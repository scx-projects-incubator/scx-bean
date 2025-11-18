package cool.scx.bean.provider;

import cool.scx.bean.BeanFactory;
import cool.scx.bean.BeanResolutionContext;
import cool.scx.bean.exception.BeanCreationException;

/// Bean 提供器
///
/// @author scx567888
/// @version 0.0.1
public interface BeanProvider {

    /// 获取 Bean
    Object getBean(BeanFactory beanFactory, BeanResolutionContext beanResolutionContext) throws BeanCreationException;

    /// 获取 Bean 的类型
    Class<?> beanClass();

    /// 表示 getBean() 是否始终返回同一实例
    boolean singleton();

}
