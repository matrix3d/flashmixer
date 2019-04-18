import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;

/**
 * created by lizhi
 */
public class JSONNode {
    public Object value;
    public JSONNode(String str){
        Gson gson=new GsonBuilder().disableHtmlEscaping().create();
        value=gson.fromJson(str, HashMap.class);
    }

    public JSONNode(Object a){
        this.value=a;
    }

    public JSONNode get(String name){
        if(value instanceof HashMap){
            return new JSONNode(((HashMap) value).get(name));
        }else if(value instanceof LinkedTreeMap){
            return new JSONNode(((LinkedTreeMap) value).get(name));
        }
        return null;
    }
}
