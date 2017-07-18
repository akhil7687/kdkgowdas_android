package kdkgowdas.com;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by akhil on 9/7/17.
 */

public class PullUserData extends AsyncTask<Void,Void,Integer> {
    Context context;

    public  PullUserData(Context context){
        this.context = context;

    }

    protected void onPreExecute (){
        super.onPreExecute();
    }

    protected Integer doInBackground(Void...arg0) {

        String updated_cookie = getCookie("www.kdkgowdas.com","updated_at");

        if(updated_cookie == null){
            return 0;
        }
        Log.d("Cookie updated at",updated_cookie);

        final SharedPrefManager pref = new SharedPrefManager(context);
        String updated_at = pref.getString("updated_at");

        if(updated_cookie.equals(updated_at)){
            return 0;
        }

        String user_id = getCookie("www.kdkgowdas.com","user_id");
        String url = "http://www.kdkgowdas.com/get_user_data.json?id="+user_id;
        Log.d("url",url);
        try {
            String user_data = getJSON(url,60*1000);
            Log.d("USER DATA",user_data);
            JSONObject response = new JSONObject(user_data);
            pref.setInt("user_id", response.getInt("user_id"));
            pref.setString("user_name",response.getString("user_name"));
            pref.setInt("family_id",response.getInt("family_id"));
            pref.setString("family_name",response.getString("family_name"));
            pref.setString("user_slug",response.getString("user_slug"));
            pref.setString("family_slug",response.getString("family_slug"));
            pref.setString("photo_url","https://www.kdkgowdas.com"+response.getString("photo_url"));
            pref.setString("updated_at",updated_cookie);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    protected void onPostExecute(Integer result) {
        if(result == 1){
            MainActivity m = (MainActivity)context;
            m.update_profile();
        }
    }

    public String getCookie(String siteName,String CookieName){
        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if (cookies == null){
            return  null;
        }
        String[] temp=cookies.split(";");
        for (String ar1 : temp ){
            if(ar1.contains(CookieName)){
                String[] temp1=ar1.split("=");
                CookieValue = temp1[1];
                break;
            }
        }
        return CookieValue;
    }


    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setRequestProperty("Authorization", "Token fdf62233577a69c657334cc0adb2bc35");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (Exception ex) {
            Log.d("Akhi Eror", "Error -- "+ex.getMessage());
        }finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
}
