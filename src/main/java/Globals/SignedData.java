package Globals;

public class SignedData implements java.io.Serializable{
    private String signature;
    private Object data;

    public SignedData(String signature, Object data) {
        this.signature = signature;
        this.data = data;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Signed Data: " + signature;
    }
}
