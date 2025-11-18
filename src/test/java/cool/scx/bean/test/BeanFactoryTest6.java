package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeanFactoryTest6 {

    public static void main(String[] args) throws BeanCreationException, NoSuchConstructorException, DuplicateBeanNameException, NoSuchBeanException, NoUniqueBeanException, NoUniqueConstructorException, IllegalBeanClassException {
        test1();
        test2();
        test3();
        test4();
        test5();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A.class);
        beanFactory.registerBeanClass("b", B.class);
        beanFactory.registerBeanClass("c", C.class);
        beanFactory.registerBeanClass("d", D.class);
        beanFactory.registerBeanClass("e", E.class);

        var a = beanFactory.getBean(A.class);
        var b = beanFactory.getBean(B.class);
        var c = beanFactory.getBean(C.class);
        var d = beanFactory.getBean(D.class);
        var e = beanFactory.getBean(E.class);

        //单例循环依赖 可以解决
        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.c, c);
        Assert.assertEquals(c.d, d);
        Assert.assertEquals(d.e, e);
        Assert.assertEquals(e.a, a);

    }

    @Test
    public static void test2() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A.class, false);
        beanFactory.registerBeanClass("b", B.class);
        beanFactory.registerBeanClass("c", C.class);
        beanFactory.registerBeanClass("d", D.class);
        beanFactory.registerBeanClass("e", E.class);

        var a = beanFactory.getBean(A.class);
        var b = beanFactory.getBean(B.class);
        var c = beanFactory.getBean(C.class);
        var d = beanFactory.getBean(D.class);
        var e = beanFactory.getBean(E.class);

        //循环链中至少有一个单例 可以解决
        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.c, c);
        Assert.assertEquals(c.d, d);
        Assert.assertEquals(d.e, e);

        //因为 a 是多例的 这里应该不同
        Assert.assertNotEquals(e.a, a);

    }

    @Test
    public static void test3() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A.class, false);
        beanFactory.registerBeanClass("b", B.class, false);
        beanFactory.registerBeanClass("c", C.class, false);
        beanFactory.registerBeanClass("d", D.class, false);
        beanFactory.registerBeanClass("e", E.class);

        var a = beanFactory.getBean(A.class);
        var b = beanFactory.getBean(B.class);
        var c = beanFactory.getBean(C.class);
        var d = beanFactory.getBean(D.class);
        var e = beanFactory.getBean(E.class);

        //循环链中至少有一个单例 可以解决
        Assert.assertNotEquals(a.b, b);
        Assert.assertNotEquals(b.c, c);
        Assert.assertNotEquals(c.d, d);

        //只有 e 是单例的 这里应该相同
        Assert.assertEquals(d.e, e);

        Assert.assertNotEquals(e.a, a);

    }

    @Test
    public static void test4() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A.class, false);
        beanFactory.registerBeanClass("b", B.class, false);
        beanFactory.registerBeanClass("c", C.class, false);
        beanFactory.registerBeanClass("d", D.class, false);
        beanFactory.registerBeanClass("e", E.class, false);

        //全是多例 不可解决
        Assert.assertThrows(BeanCreationException.class, () -> {
            var a = beanFactory.getBean(A.class);
        });
        Assert.assertThrows(BeanCreationException.class, () -> {
            var b = beanFactory.getBean(B.class);
        });
        Assert.assertThrows(BeanCreationException.class, () -> {
            var c = beanFactory.getBean(C.class);
        });
        Assert.assertThrows(BeanCreationException.class, () -> {
            var d = beanFactory.getBean(D.class);
        });
        Assert.assertThrows(BeanCreationException.class, () -> {
            var e = beanFactory.getBean(E.class);
        });

    }

    @Test
    public static void test5() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("w", W.class);
        beanFactory.registerBeanClass("t", T.class);

        Assert.assertThrows(BeanCreationException.class, () -> {
            W bean = beanFactory.getBean(W.class);
        });

    }

    public static class A {

        @Autowired
        public B b;

    }

    public static class B {

        @Autowired
        public C c;

    }

    public static class C {

        @Autowired
        public D d;

    }

    public static class D {

        @Autowired
        public E e;

    }

    public static class E {

        @Autowired
        public A a;

    }

    public static class W {
        public W(T t) {

        }
    }

    public static class T {
        public T(T t1) {
        }
    }

}
