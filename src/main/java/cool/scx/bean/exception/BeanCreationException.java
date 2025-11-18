package cool.scx.bean.exception;

/// Bean 创建异常
///
/// @author scx567888
/// @version 0.0.1
public class BeanCreationException extends RuntimeException {

    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
