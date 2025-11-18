package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeanFactoryTest3 {

    public static void main(String[] args) throws BeanCreationException, NoSuchConstructorException, DuplicateBeanNameException, NoSuchBeanException, NoUniqueBeanException, NoUniqueConstructorException, IllegalBeanClassException {
        testSingletonFieldCycle();
        testMixedScopeCycle();
        testCrossStageDependency();
    }

    @Test
    public static void testSingletonFieldCycle() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        BeanFactoryImpl factory = new BeanFactoryImpl();
        factory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(factory));

        // 注册单例 Bean
        factory.registerBeanClass("A", A.class, true);
        factory.registerBeanClass("B", B.class, true);

        A a = factory.getBean(A.class);
        B b = factory.getBean(B.class);

        // 验证循环引用是否为同一实例
        Assert.assertSame(a.b, b);
        Assert.assertSame(b.a, a);
        Assert.assertSame(a.b.a, a); // 多层引用一致性
    }

    @Test
    public static void testMixedScopeCycle() throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException, NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        BeanFactoryImpl factory = new BeanFactoryImpl();
        factory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(factory));

        // 注册不同作用域的 Bean
        factory.registerBeanClass("singleton", Singleton.class, true);
        factory.registerBeanClass("prototype", Prototype.class, false); // 多例

        Singleton s = factory.getBean(Singleton.class);
        Prototype p1 = s.prototype;
        Prototype p2 = factory.getBean(Prototype.class);

        // 验证作用域
        Assert.assertNotSame(p1, p2); // 多例应不同
        Assert.assertSame(p1.singleton, s); // 原型中的单例引用应为同一实例
        Assert.assertSame(p2.singleton, s);
    }

    @Test
    public static void testCrossStageDependency() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        BeanFactoryImpl factory = new BeanFactoryImpl();
        factory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(factory));

        factory.registerBeanClass("Alpha", Alpha.class, true);
        factory.registerBeanClass("Beta", Beta.class, true);

        Assert.assertThrows(BeanCreationException.class, () -> {
            factory.getBean(Alpha.class); // 应检测到循环
        });
    }

    // Bean 定义
    public static class A {
        @Autowired
        public B b;
    }

    public static class B {
        @Autowired
        public A a;
    }

    // Bean 定义
    public static class Singleton {
        @Autowired
        public Prototype prototype; // 依赖多例
    }

    public static class Prototype {
        @Autowired
        public Singleton singleton; // 依赖单例
    }

    // Bean 定义
    public static class Alpha {
        public Alpha(Beta beta) {
        } // 构造依赖 Beta
    }

    public static class Beta {
        @Autowired
        public Alpha alpha; // 字段依赖 Alpha
    }

}
