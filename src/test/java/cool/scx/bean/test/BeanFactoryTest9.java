package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeanFactoryTest9 {

    public static void main(String[] args) throws BeanCreationException, NoSuchConstructorException, DuplicateBeanNameException, NoSuchBeanException, NoUniqueBeanException, NoUniqueConstructorException, IllegalBeanClassException {
        test1();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));
        beanFactory.registerBeanClass("a", A.class);
        beanFactory.registerBeanClass("b", B.class);
        beanFactory.registerBeanClass("c", C.class);
        beanFactory.registerBeanClass("w", W.class);

        W bean = beanFactory.getBean(W.class);
        Assert.assertNotNull(bean);
        Assert.assertNotNull(bean.a);
        Assert.assertNotNull(bean.b);
        Assert.assertNotNull(bean.c);

        Assert.assertEquals(bean.a.a, bean.b);
        Assert.assertEquals(bean.c, bean.a.c);
        Assert.assertEquals(bean.a, bean.c.a);
    }

    public static class A {

        @Autowired
        public B a;

        @Autowired
        public C c;

    }

    public static class B {

        @Autowired
        public C b;

    }

    public static class C {

        @Autowired
        public A a;

    }


    public record W(A a, B b, C c) {

    }

}
