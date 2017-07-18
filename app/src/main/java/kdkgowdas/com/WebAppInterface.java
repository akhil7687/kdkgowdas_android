package kdkgowdas.com;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Created by akhil on 8/7/17.
 */

public class WebAppInterface {
    MainActivity mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(MainActivity c, WebView mweb) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void fill_details(String user_id,String user_name,String family_id,String family_name,String pic_url,String user_slug,String family_slug) {

    }
}