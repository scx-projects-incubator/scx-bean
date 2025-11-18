package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeanFactoryTest8 {

    public static void main(String[] args) throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        testSelfCircularDependency();
        testSimpleCircularDependency();
        testMixedScopeDependency();
        testDeepCircularDependency();
        testConstructorCircularDependencyFail();
        testFieldAndConstructorMixedFail();
        testSingletonDependsOnPrototype();
    }

    @Test
    public static void testSelfCircularDependency() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A.class);

        Assert.assertThrows(BeanCreationException.class, () -> {
            var a = beanFactory.getBean(A.class);
        });
    }

    @Test
    public static void testSimpleCircularDependency() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A1.class);
        beanFactory.registerBeanClass("b", B1.class);

        var a = beanFactory.getBean(A1.class);
        var b = beanFactory.getBean(B1.class);

        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.a, a);
    }

    @Test
    public static void testMixedScopeDependency() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A2.class);
        beanFactory.registerBeanClass("b", B2.class, false); // B2 是多例
        beanFactory.registerBeanClass("c", C2.class);

        var a = beanFactory.getBean(A2.class);
        var b = beanFactory.getBean(B2.class);
        var c = beanFactory.getBean(C2.class);

        Assert.assertEquals(a.b.c, c);
        Assert.assertEquals(c.a, a);

        Assert.assertNotEquals(beanFactory.getBean(B2.class), b); // 多例检查
    }

    @Test
    public static void testDeepCircularDependency() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A3.class);
        beanFactory.registerBeanClass("b", B3.class);
        beanFactory.registerBeanClass("c", C3.class);
        beanFactory.registerBeanClass("d", D3.class);
        beanFactory.registerBeanClass("e", E3.class);
        beanFactory.registerBeanClass("f", F3.class);

        var a = beanFactory.getBean(A3.class);
        var b = beanFactory.getBean(B3.class);
        var c = beanFactory.getBean(C3.class);
        var d = beanFactory.getBean(D3.class);
        var e = beanFactory.getBean(E3.class);
        var f = beanFactory.getBean(F3.class);

        Assert.assertEquals(a.b, b);
        Assert.assertEquals(b.c, c);
        Assert.assertEquals(c.d, d);
        Assert.assertEquals(d.e, e);
        Assert.assertEquals(e.f, f);
        Assert.assertEquals(f.a, a); // 环回来
    }

    @Test
    public static void testConstructorCircularDependencyFail() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("x", X4.class);
        beanFactory.registerBeanClass("y", Y4.class);

        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(X4.class);
        });
    }

    @Test
    public static void testFieldAndConstructorMixedFail() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("p", P5.class);
        beanFactory.registerBeanClass("q", Q5.class);
        beanFactory.registerBeanClass("r", R5.class);

        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(P5.class);
        });
    }

    @Test
    public static void testSingletonDependsOnPrototype() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("a", A6.class);
        beanFactory.registerBeanClass("b", B6.class, false); // B6 多例

        var a = beanFactory.getBean(A6.class);

        Assert.assertNotNull(a.b);
        var anotherB = beanFactory.getBean(B6.class);

        Assert.assertNotEquals(a.b, anotherB); // 确保 A 中持有的 B 和容器新取的不一样
    }

    public static class A {

        public A(A a) {

        }

    }

    // 测试类定义们
    public static class A1 {
        @Autowired
        public B1 b;
    }

    public static class B1 {
        @Autowired
        public A1 a;
    }

    public static class A2 {
        @Autowired
        public B2 b;
    }

    public static class B2 {
        @Autowired
        public C2 c;
    }

    public static class C2 {
        @Autowired
        public A2 a;
    }

    public static class A3 {
        @Autowired
        public B3 b;
    }

    public static class B3 {
        @Autowired
        public C3 c;
    }

    public static class C3 {
        @Autowired
        public D3 d;
    }

    public static class D3 {
        @Autowired
        public E3 e;
    }

    public static class E3 {
        @Autowired
        public F3 f;
    }

    public static class F3 {
        @Autowired
        public A3 a;
    }

    public static class X4 {
        public final Y4 y;

        public X4(Y4 y) {
            this.y = y;
        }
    }

    public static class Y4 {
        public final X4 x;

        public Y4(X4 x) {
            this.x = x;
        }
    }

    public static class P5 {
        @Autowired
        public Q5 q;
    }

    public static class Q5 {
        public final R5 r;

        public Q5(R5 r) {
            this.r = r;
        }
    }

    public static class R5 {
        @Autowired
        public P5 p;
    }

    public static class A6 {
        @Autowired
        public B6 b;
    }

    public static class B6 {
    }
}
