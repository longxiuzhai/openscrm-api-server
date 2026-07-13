package cn.openscrm.api.common.exception;

import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.wework.WeWorkApiException;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBadRequest(Exception exception) {
        log.error("异常{},{}", exception.getMessage(),exception);
        return ApiResponse.fail(ErrorCode.INVALID_PARAMS.getCode(), exception.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBizException(BizException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.error("异常{},{}", exception.getMessage(),exception);
        return ApiResponse.fail(errorCode.getCode(), exception.getMessage());
    }

    @ExceptionHandler(WeWorkApiException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleWeWorkException(WeWorkApiException exception) {
        log.error("异常{},{}", exception.getMessage(),exception);
        return ApiResponse.fail(exception.getErrCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("异常{},{}", exception.getMessage(),exception);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), exception.getMessage());
    }
}
