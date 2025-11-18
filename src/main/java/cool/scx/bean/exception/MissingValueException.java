package cool.scx.bean.exception;

/// 需要注入的 value 不存在
///
/// @author scx567888
/// @version 0.0.1
public class MissingValueException extends RuntimeException {

    public MissingValueException(String message) {
        super(message);
    }

}
