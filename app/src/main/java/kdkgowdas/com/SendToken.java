package kdkgowdas.com;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by akhil on 11/7/17.
 */

public class SendToken extends AsyncTask<Void , Void ,Void> {
    Context context;
    public SendToken(Context context){
        this.context = context;
    }
    protected Void doInBackground(Void...arg0) {
        SharedPrefManager sf = new SharedPrefManager(context);
        String token = sf.getString("app_token");
        if(token!=null) {
            FireIDService.sendRegistrationToServer(context,token);
        }
        return null;
    }
}
