package cool.scx.bean.exception;

/// 重复的 Bean 名称
///
/// @author scx567888
/// @version 0.0.1
public class DuplicateBeanNameException extends RuntimeException {

    public DuplicateBeanNameException(String message) {
        super(message);
    }

}
