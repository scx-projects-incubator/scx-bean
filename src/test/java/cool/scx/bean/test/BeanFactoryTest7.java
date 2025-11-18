package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.annotation.PreferredConstructor;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeanFactoryTest7 {

    public static void main(String[] args) throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        testConstructorLoop();
        testMixedInjectionLoop();
        testComplexLoop();
        testLazyInjection();
    }

    @Test
    public static void testConstructorLoop() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("x", X.class);
        beanFactory.registerBeanClass("y", Y.class);

        // 构造器注入循环, 应该直接抛出异常
        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(X.class);
        });
    }

    @Test
    public static void testMixedInjectionLoop() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("m", M.class);
        beanFactory.registerBeanClass("n", N.class);

        // 混合构造器注入 + 字段注入, 形成死循环, 应报错
        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(M.class);
        });
    }

    @Test
    public static void testComplexLoop() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("p", P.class);
        beanFactory.registerBeanClass("q", Q.class);
        beanFactory.registerBeanClass("r", R.class);
        beanFactory.registerBeanClass("s", S.class);

        // 四个Bean, 字段注入+构造器注入混合循环
        Assert.assertThrows(BeanCreationException.class, () -> {
            P bean = beanFactory.getBean(P.class);
            System.out.println(bean);
        });
    }

    @Test
    public static void testLazyInjection() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        beanFactory.registerBeanClass("lazyA", LazyA.class);
        beanFactory.registerBeanClass("lazyB", LazyB.class);

        // LazyB 延迟注入, 不应引起循环问题
        var lazyA = beanFactory.getBean(LazyA.class);
        Assert.assertNotNull(lazyA.lazyB);
    }

    public static class X {
        public final Y y;

        @PreferredConstructor
        public X(Y y) {
            this.y = y;
        }
    }

    public static class Y {
        public final X x;

        @PreferredConstructor
        public Y(X x) {
            this.x = x;
        }
    }

    public static class M {
        public final N n;

        @PreferredConstructor
        public M(N n) {
            this.n = n;
        }
    }

    public static class N {
        @Autowired
        public M m;
    }

    public static class P {
        @Autowired
        public Q q;
    }

    public static class Q {
        public final R r;

        @PreferredConstructor
        public Q(R r) {
            //这里 r 不允许是 半成品对象
            this.r = r;
        }
    }

    public static class R {
        @Autowired
        public S s;
    }

    public static class S {
        @Autowired
        public P p;
    }

    public static class LazyA {
        @Autowired
        public LazyB lazyB;
    }

    public static class LazyB {
        public String doSomething() {
            return "ok";
        }
    }
}

