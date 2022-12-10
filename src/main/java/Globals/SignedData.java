package Globals;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;


@Document(collection = "tokens", schemaVersion= "1.0")

public class SignedData implements java.io.Serializable{

    @Id
    private String signature;
    private Object data;

    public SignedData(){}

    public SignedData(String signature, Object data) {
        this.signature = signature;
        this.data = data;
    }

    @JsonGetter
    public String getSignature() {
        return signature;
    }

    @JsonSetter
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @JsonGetter
    public Object getData() {
        return data;
    }

    @JsonSetter
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return signature;
    }

    public String toShortString() {
        if(signature.length() > 20){
            return signature.substring(0, 20) + "...";
        } else {
            return signature;
        }
    }

    public String toSpecialString() {
        if(signature.length() > 20){
            return signature.substring(0, 10) + "..." + signature.substring(signature.length()-10);
        } else {
            return signature;
        }
    }

    public String toLongString() {
        return signature;
    }
}
