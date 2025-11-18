package cool.scx.bean.exception;

/// 非法的 bean 类型 比如说 接口
///
/// @author scx567888
/// @version 0.0.1
public class IllegalBeanClassException extends RuntimeException {

    public IllegalBeanClassException(String message) {
        super(message);
    }

}
