package cool.scx.bean.test;

import cool.scx.bean.BeanFactory;
import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.annotation.Value;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.dependency_resolver.ValueAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class BeanFactoryTest {

    public static void main(String[] args) throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException, NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        test1();
    }

    @Test
    public static void test1() throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException, NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new ValueAnnotationDependencyResolver(Map.of("key1", "Hello", "key2", "12345")));
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        registerBeans(beanFactory);

        // 测试 1: 单例正常依赖
        A a = beanFactory.getBean(A.class);
        Assert.assertNotNull(a);
        Assert.assertNotNull(a.b);
        Assert.assertNotNull(a.b.c);

        // 测试 2: 字段循环依赖
        D d = beanFactory.getBean(D.class);
        Assert.assertNotNull(d);
        Assert.assertNotNull(d.e);
        Assert.assertNotNull(d.e.f);
        Assert.assertNotNull(d.e.f.d);

        // 测试 3: 构造器循环依赖, 应该抛异常
        boolean exceptionThrown = false;
        try {
            beanFactory.getBean(G.class);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // 测试 4: 多例正常依赖
        J j1 = beanFactory.getBean(J.class);
        J j2 = beanFactory.getBean(J.class);
        Assert.assertNotSame(j1, j2);
        Assert.assertNotNull(j1.k);
        Assert.assertNotNull(j2.k);

        // 测试 5: 多例循环依赖, 应该抛异常
        exceptionThrown = false;
        try {
            beanFactory.getBean(M.class);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // 测试 6: 单例依赖多例
        P p = beanFactory.getBean(P.class);
        Assert.assertNotNull(p);
        Assert.assertNotNull(p.q);

        // 测试 7: 多例依赖单例
        R r1 = beanFactory.getBean(R.class);
        R r2 = beanFactory.getBean(R.class);
        Assert.assertNotSame(r1, r2);
        Assert.assertSame(r1.s, r2.s);

        // 测试 8: 混合注入
        T t = beanFactory.getBean(T.class);
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.u);

        // 测试 9: 无依赖 Bean
        W w = beanFactory.getBean(W.class);
        X x = beanFactory.getBean(X.class);
        Assert.assertNotNull(w);
        Assert.assertNotNull(x);

        // 测试 10: @Value 注入
        Y y = beanFactory.getBean(Y.class);
        Z z = beanFactory.getBean(Z.class);
        Assert.assertEquals(y.key1, "Hello");
        Assert.assertEquals(z.key2, 12345);
    }

    private static void registerBeans(BeanFactory beanFactory) throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        // 单例注册
        beanFactory.registerBeanClass("a", A.class);
        beanFactory.registerBeanClass("b", B.class);
        beanFactory.registerBeanClass("c", C.class);
        beanFactory.registerBeanClass("d", D.class);
        beanFactory.registerBeanClass("e", E.class);
        beanFactory.registerBeanClass("f", F.class);
        beanFactory.registerBeanClass("g", G.class);
        beanFactory.registerBeanClass("h", H.class);
        beanFactory.registerBeanClass("i", I.class);
        beanFactory.registerBeanClass("p", P.class);
        beanFactory.registerBeanClass("q", Q.class);
        beanFactory.registerBeanClass("s", S.class);
        beanFactory.registerBeanClass("t", T.class);
        beanFactory.registerBeanClass("u", U.class);
        beanFactory.registerBeanClass("v", V.class);
        beanFactory.registerBeanClass("w", W.class);
        beanFactory.registerBeanClass("x", X.class);
        beanFactory.registerBeanClass("y", Y.class);
        beanFactory.registerBeanClass("z", Z.class);

        // 多例注册
        beanFactory.registerBeanClass("j", J.class, false);
        beanFactory.registerBeanClass("k", K.class, false);
        beanFactory.registerBeanClass("l", L.class, false);
        beanFactory.registerBeanClass("m", M.class, false);
        beanFactory.registerBeanClass("n", N.class, false);
        beanFactory.registerBeanClass("o", O.class, false);
        beanFactory.registerBeanClass("r", R.class, false);
    }

    // =========== 内部静态类（Bean定义） ===========

    public static class A {
        @Autowired
        public B b;
    }

    public static class B {
        @Autowired
        public C c;
    }

    public static class C {
    }

    public static class D {
        @Autowired
        public E e;
    }

    public static class E {
        @Autowired
        public F f;
    }

    public static class F {
        @Autowired
        public D d;
    }

    public static class G {
        public G(H h) {
        }
    }

    public static class H {
        public H(I i) {
        }
    }

    public static class I {
        public I(G g) {
        }
    }

    public static class J {
        @Autowired
        public K k;
    }

    public static class K {
        @Autowired
        public L l;
    }

    public static class L {
    }

    public static class M {
        @Autowired
        public N n;
    }

    public static class N {
        @Autowired
        public O o;
    }

    public static class O {
        @Autowired
        public M m;
    }

    public static class P {
        @Autowired
        public Q q;
    }

    public static class Q {
    }

    public static class R {
        @Autowired
        public S s;
    }

    public static class S {
    }

    public static class T {
        @Autowired
        public U u;

        public T(V v) {
        }
    }

    public static class U {
    }

    public static class V {
    }

    public static class W {
    }

    public static class X {
    }

    public static class Y {
        @Value("key1")
        public String key1;
    }

    public static class Z {
        public final int key2;

        public Z(@Value("key2") int key2) {
            this.key2 = key2;
        }
    }

}
