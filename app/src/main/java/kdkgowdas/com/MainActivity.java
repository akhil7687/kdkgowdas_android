package kdkgowdas.com;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_CODE = 200;
    WebView webView;
    SwipeRefreshLayout refreshLayout;
    DrawerLayout drawer;
    Toolbar toolbar;
    NavigationView navigationView;

    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }



        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.getContentInsetEnd();
        toolbar.setPadding(10, 0, 10, 0);
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);

        setTitle(null);

        webView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);


        if(Build.VERSION.SDK_INT >= 21){
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT < 19){
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.setWebViewClient(new Callback());



        webView.setWebChromeClient(new WebChromeClient(){
            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FCR);
            }
            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FCR);
            }
            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
            }
            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams){
                if(mUMA != null){
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    }catch(IOException ex){
                        Log.e("AKHIL", "Image file creation failed", ex);
                    }
                    if(photoFile != null){
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    }else{
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if(takePictureIntent != null){
                    intentArray = new Intent[]{takePictureIntent};
                }else{
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }
        });



        webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString()+" KDKAPP");

        if(getIntent().getStringExtra("url")!=null && getIntent().getStringExtra("url").contains("http")){
            webView.loadUrl(getIntent().getStringExtra("url"));
        }else {
            webView.loadUrl("https://www.kdkgowdas.com/updates");
        }

        toolbar.setTitle(null);

         refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh) ;


        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        webView.reload();
                    }
                }
        );


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        addActions();
        update_profile();

        (new SendToken(this)).execute();

        //registerReceiver(cbr, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void addActions(){
        ImageButton toggle_btn = (ImageButton)toolbar.findViewById(R.id.toggle_btn);
        toggle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
            }
        });

        ImageButton feed_btn = (ImageButton)toolbar.findViewById(R.id.feed_btn);
        feed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setSelected(true);
                load_url("https://www.kdkgowdas.com/updates");
            }
        });

        ImageButton members_btn = (ImageButton)toolbar.findViewById(R.id.members_btn);
        members_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               load_url("https://www.kdkgowdas.com/members");
            }
        });

        ImageButton families_btn = (ImageButton)toolbar.findViewById(R.id.families_btn);
        families_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load_url("https://www.kdkgowdas.com/families");
            }
        });

        ImageButton articles_btn = (ImageButton)toolbar.findViewById(R.id.articles_btn);
        articles_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               load_url("https://www.kdkgowdas.com/articles");
            }
        });


        ImageButton classifieds_btn = (ImageButton)toolbar.findViewById(R.id.classified_btn);
        classifieds_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               load_url("https://www.kdkgowdas.com/classifieds");
            }
        });

        ImageButton home_btn = (ImageButton)toolbar.findViewById(R.id.home_btn);
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load_url("https://www.kdkgowdas.com");
            }
        });


    }


    public void load_url(String url){
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } if(webView.canGoBack()){
            webView.goBack();
        }else{
            new AlertDialog.Builder(new android.view.ContextThemeWrapper(this, R.style.popup_theme))
                    .setTitle("EXIT")
                    .setMessage("Are you sure you want to close?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.inbox_menu) {
            String url = "https://www.kdkgowdas.com/inbox";
            load_url(url);
        } else if (id == R.id.my_porifle_menu) {
            SharedPrefManager sf = new SharedPrefManager(this);
            String slug = sf.getString("user_slug");
            if(slug!=null) {
                String url = "https://www.kdkgowdas.com/member/" + slug;
                load_url(url);
            }
        } else if (id == R.id.my_family_menu) {
            SharedPrefManager sf = new SharedPrefManager(this);
            String slug = sf.getString("family_slug");
            if(slug!=null) {
                String url = "https://www.kdkgowdas.com/families/" + slug;
                load_url(url);
            }
        } else if (id == R.id.news_events) {
            String url = "https://www.kdkgowdas.com/events";
            load_url(url);
        }else if (id == R.id.notification_menu) {
            String url = "https://www.kdkgowdas.com/notifications";
            load_url(url);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }


    public void showProgress(){

        refreshLayout.setRefreshing(true);
    }

    public void change_image_selected(){
        Log.d("Selected",webView.getUrl());
        if(webView.getUrl().equals("https://www.kdkgowdas.com/")){
            ((ImageButton)toolbar.findViewById(R.id.home_btn)).setImageResource(R.drawable.ic_home_white_24dp_selected);
        }else{
            ((ImageButton)toolbar.findViewById(R.id.home_btn)).setImageResource(R.drawable.ic_home_white_24dp);
        }

        if(webView.getUrl().contains("updates")){
            ((ImageButton)toolbar.findViewById(R.id.feed_btn)).setImageResource(R.drawable.ic_cast_white_24dp_selected);
        }else{
            ((ImageButton)toolbar.findViewById(R.id.feed_btn)).setImageResource(R.drawable.ic_cast_white_24dp);
        }

        if(webView.getUrl().contains("family") || webView.getUrl().contains("families")){
            ((ImageButton)toolbar.findViewById(R.id.families_btn)).setImageResource(R.drawable.ic_families_selected);
        }else{
            ((ImageButton)toolbar.findViewById(R.id.families_btn)).setImageResource(R.drawable.ic_families);
        }


        if(webView.getUrl().contains("articles")){
            ((ImageButton)toolbar.findViewById(R.id.articles_btn)).setImageResource(R.drawable.ic_chrome_reader_mode_white_24dp_selected);
        }else{
            ((ImageButton)toolbar.findViewById(R.id.articles_btn)).setImageResource(R.drawable.ic_chrome_reader_mode_white_24dp);
        }
        if(webView.getUrl().contains("classifieds")){
            ((ImageButton)toolbar.findViewById(R.id.classified_btn)).setImageResource(R.drawable.ic_shopping_basket_white_24dp_selected);
        }else{
            ((ImageButton)toolbar.findViewById(R.id.classified_btn)).setImageResource(R.drawable.ic_shopping_basket_white_24dp);
        }
        if(webView.getUrl().contains("member")){
            ((ImageButton)toolbar.findViewById(R.id.members_btn)).setImageResource(R.drawable.ic_members_selected);
        }else{
            ((ImageButton)toolbar.findViewById(R.id.members_btn)).setImageResource(R.drawable.ic_members);
        }
    }

    public void hideProgress(){
        change_image_selected();
        refreshLayout.setRefreshing(false);
        new PullUserData(MainActivity.this).execute();
    }


    public void update_profile(){
        new SendToken(this).execute();
        try{
            SharedPrefManager sf = new SharedPrefManager(MainActivity.this);
            if(sf.getInt("user_id") == -1){
                return;
            }
            TextView name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.profile_name);
            name.setText(sf.getString("user_name"));
            CircularImageView profile = (CircularImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_pic);
            String url = (sf.getString("photo_url"));
            Glide.with(MainActivity.this)
                    .load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(profile);
            TextView fam_text = (TextView)navigationView.getHeaderView(0).findViewById(R.id.family_name);
            fam_text.setText(sf.getString("family_name"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkPermission() {
        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
        if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean read_ext = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted && read_ext)
                        Toast.makeText(MainActivity.this,"Thank you",Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(MainActivity.this,"You must allow these permissions to use this app",Toast.LENGTH_LONG).show();
                        checkPermission();
                    }
                }

                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public class Callback extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            showProgress();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            hideProgress();
            String cookies = CookieManager.getInstance().getCookie(url);
            Log.d("Finish", "All the cookies in a string:" + cookies);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // Handle the error
            Log.d("Error",errorCode+"");
            String summary = "<html><body style='background: white;'><p style='color: red; margin-top:40%; font-weight:bold; font-size:25px; text-align:center;'>Sorry! Something went wrong.<br><br></p> <p style='font-weight:bold; font-size:25px; text-align:center; color:red;'>Please check your internet connection and try again</p></body></html>";
            view.loadData(summary, "text/html", null);
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
            // Redirect to deprecated method, so you can use it in all SDK versions
            Log.d("Error","error recieved");
            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
        }
    }

}
