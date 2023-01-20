package kalaathon.com;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification{

    private String URL = "https://fcm.googleapis.com/fcm/send";

    public void sendNotification(String title, String body, String user_id, String media_id, String type, Context context) {

        RequestQueue requestQue = Volley.newRequestQueue(context);

        JSONObject json = new JSONObject();
        try {
            json.put("to","/topics/"+user_id);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",title);
            notificationObj.put("body",body);
            notificationObj.put("android_channel_id","kalaathon.com");
            notificationObj.put("sound","default");
            notificationObj.put("vibrate","true");

            if(media_id!=null) {
                JSONObject extraData = new JSONObject();
                extraData.put("type", type);
                extraData.put("user_id", user_id);
                extraData.put("media_id", media_id);
                json.put("notification",notificationObj);
                json.put("data",extraData);
            }
            else json.put("notification",notificationObj);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAUkL5sJM:APA91bEETV9PUkss-Rr2JOVpUaJMG3akNo7K5UdAg-JHSqkLsr4b0dPzjFT_tjBo9cMT6_Tb48l9MGN50TgiiNJW1Ec9A7-QRpuvPPJNYhyo64FZY2ZPgK1RkoiQ-vEwl53KC-EklDMO");
                    return header;
                }
            };
            requestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }
}
