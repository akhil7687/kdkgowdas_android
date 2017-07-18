package kdkgowdas.com;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class FireIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String tkn = FirebaseInstanceId.getInstance().getToken();
        SharedPrefManager sf = new SharedPrefManager(getApplicationContext());
        sf.setString("app_token",tkn);
        sendRegistrationToServer(getApplicationContext(),tkn);
    }

    public static void sendRegistrationToServer(Context context,String token){
        FirebaseMessaging.getInstance().subscribeToTopic("members");
        Log.d("Token sending","token sending");
        if(token == null){
            return;
        }
        Log.d("Token",token);
        SharedPrefManager sf = new SharedPrefManager(context);
        int user_id = sf.getInt("user_id");
        if (user_id == -1){
            return;
        }

        String url = "https://www.kdkgowdas.com/users/update_token.json";
        HttpURLConnection c = null;
        try {

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("user_id", ""+user_id);
            postDataParams.put("app_token", token);

            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Authorization", "Token fdf62233577a69c657334cc0adb2bc35");
            c.setReadTimeout(15000);
            c.setConnectTimeout(15000);
            c.setDoInput(true);
            c.setDoOutput(true);

            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode=c.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in=new BufferedReader(
                        new InputStreamReader(
                                c.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
                Log.d("Status", sb.toString());
            }
            else {
                Log.d("Response", responseCode+"");
            }

            Log.d("Sync","Synced");


        } catch (Exception ex) {
            Log.d("Akhi Eror", "Error -- "+ex.getMessage());
        }finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {

                }
            }
        }
    }

    public static String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

}