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

public class BeanFactoryTest2 {

    public static void main(String[] args) throws BeanCreationException, NoSuchConstructorException, NoSuchBeanException, DuplicateBeanNameException, NoUniqueBeanException, NoUniqueConstructorException, IllegalBeanClassException {
        test2();
    }

    @Test
    public static void test2() throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException, NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        var beanFactory = new BeanFactoryImpl();
        beanFactory.beanDependencyResolvers().add(new ValueAnnotationDependencyResolver(Map.of("existKey", "value")));
        beanFactory.beanDependencyResolvers().add(new AutowiredAnnotationDependencyResolver(beanFactory));

        registerExtremeBeans(beanFactory); // 注册极端测试所需的 Bean

        testMultiLevelCycleDependency(beanFactory);
        testMixedScopeCycleDependency(beanFactory);
        testConstructorFieldMixedCycle(beanFactory);
        testPrototypeCycleDependency(beanFactory);
        testMissingValueInjection(beanFactory);
        testMultipleImplInjection(beanFactory);
        testBeanCreationException(beanFactory);
    }

    //============= 测试用例方法 =============

    /**
     * 测试多层嵌套循环依赖 (单例)
     * A → B → C → A
     * 预期: 由于是字段注入, 应成功解决循环依赖
     */
    private static void testMultiLevelCycleDependency(BeanFactory beanFactory) throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        MultiA a = beanFactory.getBean(MultiA.class);
        Assert.assertNotNull(a);
        Assert.assertSame(a.b.c.a, a); // 检查循环引用是否为同一实例
    }

    /**
     * 测试混合作用域的循环依赖
     * 单例 SingletonM → 原型 PrototypeM → 单例 SingletonM
     * 预期: 由于原型每次创建新实例, 不会形成真正的循环, 应成功创建
     */
    private static void testMixedScopeCycleDependency(BeanFactory beanFactory) throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        SingletonM s = beanFactory.getBean(SingletonM.class);
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.prototype);
        // 验证原型实例不同
        Assert.assertNotSame(s.prototype, beanFactory.getBean(PrototypeM.class));
    }

    /**
     * 测试构造器 + 字段混合循环依赖
     * ConstructorCycleX 需要 ConstructorCycleY 作为构造参数
     * ConstructorCycleY 需要 ConstructorCycleX 作为字段注入
     * 预期: 抛出 BeanCreationException
     */
    private static void testConstructorFieldMixedCycle(BeanFactory beanFactory) {
        Assert.assertThrows(BeanCreationException.class, () -> {
            try {
                beanFactory.getBean(ConstructorCycleY.class);
            } catch (BeanCreationException e) {
                throw e.getCause().getCause();
            }
        });
        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(ConstructorCycleX.class);
        });
    }

    /**
     * 测试原型 Bean 之间的循环依赖
     * PrototypeP → PrototypeQ → PrototypeP
     * 预期: 每次获取都会抛出异常
     */
    private static void testPrototypeCycleDependency(BeanFactory beanFactory) {
        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(PrototypeP.class);
        });
    }

    /**
     * 测试 @Value 注入不存在的配置项
     * 预期: 抛出 InjectionException
     */
    private static void testMissingValueInjection(BeanFactory beanFactory) {
        Assert.assertThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(MissingValue.class);
        });
    }

    /**
     * 测试同一接口多个实现的注入问题
     * 使用 @Named 或 @Qualifier 解决歧义
     * 预期: 正确注入指定名称的 Bean
     */
    private static void testMultipleImplInjection(BeanFactory beanFactory) throws BeanCreationException, NoSuchBeanException, NoUniqueBeanException {
        MultiServiceUser user = beanFactory.getBean(MultiServiceUser.class);
        Assert.assertTrue(user.service instanceof PrimaryServiceImpl);
    }

    /**
     * 测试 Bean 创建时抛出异常的情况
     * 预期: 包装成 BeanCreationException 并传递原始异常
     */
    private static void testBeanCreationException(BeanFactory beanFactory) {
        Throwable cause = Assert.expectThrows(BeanCreationException.class, () -> {
            beanFactory.getBean(ErrorBean.class);
        });

        Assert.assertTrue(cause.getCause() instanceof IllegalStateException);
    }

    //============= 注册极端测试 Bean =============
    private static void registerExtremeBeans(BeanFactory beanFactory) throws NoSuchConstructorException, DuplicateBeanNameException, NoUniqueConstructorException, IllegalBeanClassException {
        // 多层循环依赖（单例）
        beanFactory.registerBeanClass("multiA", MultiA.class);
        beanFactory.registerBeanClass("multiB", MultiB.class);
        beanFactory.registerBeanClass("multiC", MultiC.class);

        // 混合作用域循环
        beanFactory.registerBeanClass("singletonM", SingletonM.class);
        beanFactory.registerBeanClass("prototypeM", PrototypeM.class, false);

        // 构造器+字段混合循环
        beanFactory.registerBeanClass("cycleX", ConstructorCycleX.class);
        beanFactory.registerBeanClass("cycleY", ConstructorCycleY.class);

        // 原型循环依赖
        beanFactory.registerBeanClass("protoP", PrototypeP.class, false);
        beanFactory.registerBeanClass("protoQ", PrototypeQ.class, false);

        // 缺失配置项测试
        beanFactory.registerBeanClass("missingValue", MissingValue.class);

        // 多实现测试
        beanFactory.registerBeanClass("primaryService", PrimaryServiceImpl.class);
        beanFactory.registerBeanClass("secondaryService", SecondaryServiceImpl.class);
        beanFactory.registerBeanClass("multiServiceUser", MultiServiceUser.class);

        // 异常 Bean
        beanFactory.registerBeanClass("errorBean", ErrorBean.class);
    }

    //============= 极端测试 Bean 定义 =============

    // 多实现测试
    public interface MultiService {}

    // 多层循环依赖
    public static class MultiA {
        @Autowired
        public MultiB b;
    }

    public static class MultiB {
        @Autowired
        public MultiC c;
    }

    public static class MultiC {
        @Autowired
        public MultiA a;
    }

    // 混合作用域循环
    public static class SingletonM {
        @Autowired
        public PrototypeM prototype;
    }

    public static class PrototypeM {
        @Autowired
        public SingletonM singleton; // 依赖单例不会形成真正循环
    }

    // 构造器+字段混合循环
    public static class ConstructorCycleX {
        // 理论上这个是创建不出来的 因为 构造函数中不应该存在半成品对象
        public ConstructorCycleX(ConstructorCycleY y) {
            Assert.assertNotNull(y.x);
        }
    }

    public static class ConstructorCycleY {
        @Autowired
        public ConstructorCycleX x;
    }

    // 原型循环依赖
    public static class PrototypeP {
        @Autowired
        public PrototypeQ q;
    }

    public static class PrototypeQ {
        @Autowired
        public PrototypeP p;
    }

    // @Value 缺失配置
    public static class MissingValue {
        @Value("notExistKey")
        public String val;
    }

    public static class PrimaryServiceImpl implements MultiService {}

    public static class SecondaryServiceImpl implements MultiService {}

    public static class MultiServiceUser {
        @Autowired("primaryService")
        public MultiService service; // 需通过名称或 @Primary 解决
    }

    // 异常 Bean
    public static class ErrorBean {
        public ErrorBean() {
            throw new IllegalStateException("模拟构造异常");
        }
    }

}
