package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.Autowired;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.dependency_resolver.ValueAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class BeanFactoryTest10 {

    public static void main(String[] args) throws BeanCreationException, NoSuchConstructorException, DuplicateBeanNameException, NoSuchBeanException, NoUniqueBeanException, NoUniqueConstructorException, IllegalBeanClassException {
        test1();
        test2();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new ValueAnnotationDependencyResolver(Map.of()));
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));
        beanFactory.registerBeanClass("a", A.class);
        beanFactory.registerBeanClass("b", B.class);
        beanFactory.registerBeanClass("c", C.class);

        var bean = beanFactory.getBean(A.class);
        Assert.assertNotNull(bean);
    }

    @Test
    public static void test2() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new ValueAnnotationDependencyResolver(Map.of()));
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));
        beanFactory.registerBeanClass("a", AAA.class);
        beanFactory.registerBeanClass("b", BBB.class);
        beanFactory.registerBeanClass("c", CCC.class);

        var bean = beanFactory.getBean(AAA.class);
        Assert.assertNotNull(bean);
    }

    public static class A<T extends B> {

        @Autowired("c")
        public T a;

        @Autowired
        public C c;

    }

    public static class B {

        @Autowired
        public C b;

    }

    public static class C extends B {

        @Autowired
        public A<?> a;

    }


    public static class AAA {

        public AAA(BBB b) {
            // 这里的 BBB 不允许是半成品,
            // 测试能否 正确获得 ccc
            Assert.assertNotNull(b.ccc);
            Assert.assertEquals(b.ccc.bbb,b);
        }

    }

    public static class BBB {

        @Autowired
        public CCC ccc;

    }

    public static class CCC  {

        @Autowired
        public BBB bbb;

    }

}
