package xg;
import com.tencent.xinge.Message;
import com.tencent.xinge.XingeApp;
import org.json.JSONObject;
public class LaoNianBaoPush {
	public static XingeApp xingeApp;
	public static JSONObject pushMessage(String account,Message message){
		xingeApp=new XingeApp(2100270770,"a3a9eab4d07e5b83fd2fc976feb6e1b4");
		JSONObject a=xingeApp.pushSingleAccount(0,account,message);
		return a;
	}
}
