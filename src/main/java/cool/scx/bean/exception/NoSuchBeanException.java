package cool.scx.bean.exception;

/// 未找到 对应的 Bean
///
/// @author scx567888
/// @version 0.0.1
public class NoSuchBeanException extends RuntimeException {

    public NoSuchBeanException(String message) {
        super(message);
    }

}
