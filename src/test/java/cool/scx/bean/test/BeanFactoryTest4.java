package cool.scx.bean.test;

import cool.scx.bean.BeanFactoryImpl;
import cool.scx.bean.annotation.PreferredConstructor;
import cool.scx.bean.dependency_resolver.AutowiredAnnotationDependencyResolver;
import cool.scx.bean.dependency_resolver.ValueAnnotationDependencyResolver;
import cool.scx.bean.exception.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class BeanFactoryTest4 {

    public static void main(String[] args) throws BeanCreationException, NoSuchConstructorException, DuplicateBeanNameException, NoSuchBeanException, NoUniqueBeanException, NoUniqueConstructorException, IllegalBeanClassException {
        test1();
    }

    @Test
    public static void test1() throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException, BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));
        beanFactory.beanDependencyResolvers().add(new ValueAnnotationDependencyResolver(Map.of("key1", "Hello", "key2", "12345")));

        beanFactory.registerBeanClass("a", A.class);
        beanFactory.registerBeanClass("b", B.class);
        // 注册阶段就会报错
        Assert.expectThrows(NoUniqueConstructorException.class, () -> {
            beanFactory.registerBeanClass("c", C.class);
        });
        beanFactory.registerBeanClass("d", D.class);
        Assert.expectThrows(NoSuchConstructorException.class, () -> {
            beanFactory.registerBeanClass("e", E.class);
        });
        Assert.expectThrows(NoUniqueConstructorException.class, () -> {
            beanFactory.registerBeanClass("f", F.class);
        });

        //正常获取
        A a = beanFactory.getBean(A.class);
        B b = beanFactory.getBean(B.class);

        Assert.expectThrows(NoSuchBeanException.class, () -> {
            C c = beanFactory.getBean(C.class);
        });

        beanFactory.getBean(D.class);
        Assert.expectThrows(NoSuchBeanException.class, () -> {
            beanFactory.getBean(E.class);
        });
        Assert.expectThrows(NoSuchBeanException.class, () -> {
            beanFactory.getBean(F.class);
        });
    }

    public static class A {

    }

    public static class B {
        public B() {
        }
    }

    public static class C {
        public C() {
        }

        public C(int a) {
        }
    }

    public static class D {

        @PreferredConstructor
        public D() {

        }

        public D(int a) {

        }

    }

    public static class E {
        private E() {

        }
    }

    public static class F {
        @PreferredConstructor
        public F() {

        }

        @PreferredConstructor
        public F(int a) {

        }
    }

}
