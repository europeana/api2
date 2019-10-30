package eu.europeana.api2.v2.model;

public class EmailError {

    private String errorCode;
    private String errormsg;
    private int count;
    private long updatedTimestamp;
    private String emailMessageBody;

    public EmailError(String errorCode, String errormsg, int count, long updatedTimestamp, String emailMessageBody) {
        this.errorCode = errorCode;
        this.errormsg = errormsg;
        this.count = count;
        this.updatedTimestamp = updatedTimestamp;
        this.emailMessageBody = emailMessageBody;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(long updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public String getEmailMessageBody() { return emailMessageBody; }

    public void setEmailMessageBody(String emailMessageBody) { this.emailMessageBody = emailMessageBody; }

    @Override
    public String toString() {
        return "EmailError{" +
                "errorCode='" + errorCode + '\'' +
                ", errormsg='" + errormsg + '\'' +
                ", count=" + count +
                ", updatedTimestamp=" + updatedTimestamp +
                '}';
    }
}
