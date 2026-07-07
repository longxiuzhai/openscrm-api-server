package cn.openscrm.api.wework;

public class WeWorkApiException extends RuntimeException {

    private final int errCode;

    public WeWorkApiException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }
}
