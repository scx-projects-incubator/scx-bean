package cool.scx.bean.exception;

/// 不是唯一符合条件的 Bean
///
/// @author scx567888
/// @version 0.0.1
public class NoUniqueBeanException extends RuntimeException {

    public NoUniqueBeanException(String message) {
        super(message);
    }

}
