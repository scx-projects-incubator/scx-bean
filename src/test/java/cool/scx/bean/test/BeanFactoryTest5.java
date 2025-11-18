package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeanFactoryTest5 {

    public static void main(String[] args) throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        test1();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A.class);

        //不允许注册一个 接口
        Assert.assertThrows(IllegalBeanClassException.class, () -> {
            beanFactory.registerBeanClass("b", B.class);
        });

        //不允许注册一个 抽象类
        Assert.assertThrows(IllegalBeanClassException.class, () -> {
            beanFactory.registerBeanClass("c", C.class);
        });

        //不允许注册一个 注解
        Assert.assertThrows(IllegalBeanClassException.class, () -> {
            beanFactory.registerBeanClass("d", D.class);
        });

        //不允许非静态成员类
        Assert.assertThrows(IllegalBeanClassException.class, () -> {
            beanFactory.registerBeanClass("e", E.class);
        });

        beanFactory.registerBeanClass("f", F.class);

        //不允许注册一个 枚举
        Assert.assertThrows(IllegalBeanClassException.class, () -> {
            beanFactory.registerBeanClass("g", G.class);
        });

        beanFactory.registerBeanClass("h", H.class);

        beanFactory.initializeBeans();

        A a = beanFactory.getBean(A.class);
        F f = beanFactory.getBean(F.class);
        H h = beanFactory.getBean(H.class);

        //不应该被重复注入 因为是 final 的
        Assert.assertNull(h.a);

        var beanNames = beanFactory.getBeanNames();
        Assert.assertEquals(beanNames, new String[]{"a", "f", "h"});

        // 名称不存在
        Assert.assertThrows(NoSuchBeanException.class, () -> {
            beanFactory.getBean("no");
        });

        // 名称存在 类型不符合
        Assert.assertThrows(NoSuchBeanException.class, () -> {
            beanFactory.getBean("a", B.class);
        });

        // 支持直接注册一个已存在的 bean
        var a1 = new A();
        beanFactory.registerBean("a1", a1);

        Assert.assertThrows(DuplicateBeanNameException.class, () -> {
            beanFactory.registerBean("a1", a1);
        });

        var a2 = beanFactory.getBean("a1");
        Assert.assertEquals(a1, a2);

        // 现在有多个 A 获取应该报错
        Assert.assertThrows(NoUniqueBeanException.class, () -> {
            beanFactory.getBean(A.class);
        });

        // 支持字段注入一个 已经存在的类
        var k = new K();
        beanFactory.registerBean("k", k, true);

        K k1 = beanFactory.getBean(K.class);
        Assert.assertEquals(k, k1);

        Assert.assertEquals(k1.a, a1);

    }

    //普通类
    public static class A {

    }

    //接口
    public interface B {

    }

    //抽象类
    public abstract class C {

    }

    //注解
    public @interface D {

    }

    //内部类
    public class E {

    }

    // record
    public record F(A a) {

    }

    //枚举
    public enum G {

    }

    public static class H {

        @Autowired
        public final A a;

        public H() {
            this.a = null;
        }

    }

    public static class K {
        @Autowired("a1")
        public A a;
    }

}
