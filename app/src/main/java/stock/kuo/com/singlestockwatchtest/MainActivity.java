package stock.kuo.com.singlestockwatchtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {

    // This TAG is used for logging
    private static final String TAG = "SingleStockWatchActivity";

    // This URL string points to the Google Stock API
    //private static final String GOOGLE_STOCK_URL = "http://www.google.com/ig/api";
    //2015 available URL
    private static final String GOOGLE_STOCK_URL = "http://www.google.com/finance/info?infotype=infoquoteall&q=";
    //Yahoo Stock API : historical data csv
    private static final  String YAHOO_STOCK_CSV_URL
            = "http://ichart.yahoo.com/table.csv?s=";
    /*
    a – 起始时间，月
    b – 起始时间，日
    c – 起始时间，年
    d – 结束时间，月
    e – 结束时间，日
    f – 结束时间，年
    g – 时间周期。Example: g=w, 表示周期是’周’。d->’日’(day), w->’周’(week)，m->’月’(mouth)，v->’dividends only’
    */
    private static  final  String YAHOO_PARAM
            = "&a=<int>&b=<int>&c=<int>&d=<int>&e=<int>&f=<int>&g=w&ignore=.csv";
    //popup k-chart
    private PopupWindow mPopupWindow;
    // 屏幕的width
    private int mScreenWidth;
    // 屏幕的height
    private int mScreenHeight;
    // PopupWindow的width
    private int mPopupWindowWidth;
    // PopupWindow的height
    private int mPopupWindowHeight;

    // This URL is used when retrieving the stock activity chart image.
    private static final String GOOGLE_URL = "http://www.google.com";

    // These String constants refer to the XML elements we will be displaying
    private static final String SYMBOL = "symbol";
    private static final String COMPANY = "company";
    private static final String EXCHANGE = "exchange";
    private static final String VOLUME = "volume";
    private static final String LAST = "last";
    private static final String CHANGE = "change";
    private static final String PERC_CHANGE = "perc_change";
    private static final String CHART_URL = "/finance/getchart?q=";//2015 available for getting chart

    // This String refers to the attribute we are collecting for each element in
    // our XML
    private static final String DATA = "data";

    // This HashMap will store, in key-value pairs, the stock data we receive.
    private HashMap<String, String> hmStockData = new HashMap<String, String>();

    // This is the edit control that users will key into
    private EditText edSymbol = null;

    // This is the button that, when pressed, will request stock price data.
    private Button bnRetrieve = null;

    // This variable will hold the stock symbol value the user has keyed in.
    private String symbol = "";

    //new chart method
    private ImageView ivChart = null;
    private static Bitmap bmImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bnRetrieve = (Button) findViewById(R.id.bn_retrieve);

        ivChart = (ImageView) findViewById(R.id.img_chart);
        ivChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPopupWindowInstance();
                mPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                //mPopupWindow.update(0, 0, mPopupWindowWidth, mPopupWindowHeight);
            }
        });

        edSymbol = (EditText) findViewById(R.id.edit_symbol);

        edSymbol.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            // here we respond to users key input events
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

                // collect the text from the edit control, and trim off spaces.
                symbol = edSymbol.getText().toString().trim();

                // if the user has entered at least one character, enable the
                // bnRetrieve button.
                // otherwise, disable it.
                bnRetrieve.setEnabled(symbol.length() > 0);

            }

        });
    }

    public void retrieveQuote(View vw) {

        // our "symbol" variable already has the text from the edSymbol view via
        // the onTextChanged() event capture.
        String request = GOOGLE_STOCK_URL + symbol;//symbol:stock No.

        StockRetrieveTask task = new StockRetrieveTask();

        task.execute(new String[] { request });

    }

    private void hideKeyboard() {

        // hide the soft keyboard, if it is currently visible.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edSymbol.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

    }

    private ProgressDialog createProgressDialog(final Context context,
                                                final String message) {

        Log.i(TAG, "createProgressDialog");

        final ProgressDialog progressDialog = new ProgressDialog(context);

        progressDialog.setMessage(message);
        progressDialog.setProgressDrawable(getWallpaper());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        return progressDialog;
    }

    private void readResponse(String response) {

        Log.i(TAG, "displayResponse");

        // initialize our HashMap, resetting it if it was previously used.
        hmStockData = new HashMap<String, String>();

        try {

            String elementName = "";
            String elementValue = "";
            String nameSpace = "";

            //response handling:double slash & get json object
            response = response.substring(4, response.length()-1);

            //old xml parser turn to the json parser
            JSONObject jsonObject = new JSONObject(response);
            Iterator<?> keys = jsonObject.keys();

            while (keys.hasNext())
            {
                String key = (String)keys.next();
                String value = jsonObject.getString(key);
                hmStockData.put(key, value);
            }

            /*StringReader xmlReader = new StringReader(response);

            // The XmlPullParser responds to XML "events". As it steps through
            // the passed XML, each different element it encounters triggers an
            // event.

            // The developer's job is to respond appropriately to the desired
            // events.

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(xmlReader);

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {

                    case XmlPullParser.START_TAG:

                        elementName = parser.getName();
                        elementValue = parser.getAttributeValue(nameSpace, DATA);

                        hmStockData.put(elementName, elementValue);

                }

                eventType = parser.next();
            }*/

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }

    private void updateTextView(int id, String name) {

        TextView tvTarget = (TextView) findViewById(id);

        if (tvTarget == null) return;

        tvTarget.setText(hmStockData.containsKey(name) ? hmStockData.get(name) : "");

    }

    private void displayResponse() {

        Log.i(TAG, "displayResponse");

        //tag's name changed
        /*updateTextView(R.id.tv_symbol, SYMBOL);
        updateTextView(R.id.tv_company, COMPANY);
        updateTextView(R.id.tv_exchange, EXCHANGE);
        updateTextView(R.id.tv_volume, VOLUME);
        updateTextView(R.id.tv_last, LAST);
        updateTextView(R.id.tv_change, CHANGE);
        updateTextView(R.id.tv_perc_change, PERC_CHANGE);*/

        //new naming
        updateTextView(R.id.tv_symbol, "t");//股票代號
        updateTextView(R.id.tv_company, "name");//公司
        updateTextView(R.id.tv_open, "op");//開盤
        updateTextView(R.id.tv_l_cur, "l_cur");//成交
        updateTextView(R.id.tv_volume, "vo");//總量
        updateTextView(R.id.tv_cp, "cp_fix");//漲幅
        updateTextView(R.id.tv_high, "hi");//最高
        updateTextView(R.id.tv_low, "lo");//最低
        updateTextView(R.id.tv_c, "c_fix");//漲跌
        updateTextView(R.id.tv_pcls, "pcls_fix");//昨收
        updateTextView(R.id.tv_hi52, "hi52");//52週高價
        updateTextView(R.id.tv_lo52, "lo52");//52週低價
        updateTextView(R.id.tv_pe, "pe");//The price/earnings ratio.
        updateTextView(R.id.tv_eps, "eps");//The earnings per share.
        updateTextView(R.id.tv_shares, "shares");//The number of outstanding shares.
        updateTextView(R.id.tv_mc, "mc");//The market capitalization of the stock.
        updateTextView(R.id.tv_ccol, "ccol");//?


        ivChart.setImageBitmap(bmImg);

        //change old resolution
//        if  (hmStockData.containsKey(CHART_URL)) {

//            String chartURL = hmStockData.get(CHART_URL);

//            String googleChart = GOOGLE_URL + chartURL;

        //new url for get chart
        //String googleChart = GOOGLE_URL + CHART_URL + symbol;

        //ImageView ivChart = (ImageView) findViewById(R.id.img_chart);

        //not working at new android version
        /*try {
            Log.i(TAG, "Chart bitmap from URL");

            URL googleChartURL = new URL(googleChart);
            HttpURLConnection conn = (HttpURLConnection) googleChartURL.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();

            Bitmap bmImg = BitmapFactory.decodeStream(is);

            ivChart.setImageBitmap(bmImg);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);

        }*/

//        }

    }

    private void chartResponse()
    {
        String googleChart = GOOGLE_URL + CHART_URL + symbol;

        try {
            Log.i(TAG, "Chart bitmap from URL");

            URL googleChartURL = new URL(googleChart);
            HttpURLConnection conn = (HttpURLConnection) googleChartURL.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();

            bmImg = BitmapFactory.decodeStream(is);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);

        }
    }

    private class StockRetrieveTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "StockRetrieveTask";

        private ProgressDialog pDlg = null;

        @Override
        protected void onPreExecute() {

            Log.i(TAG, "onPreExecute");

            hideKeyboard();

            pDlg = createProgressDialog(MainActivity.this,
                    getString(R.string.retrieving));

            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {

            Log.i(TAG, "doInBackground");

            //thread resolution
            chartResponse();

            StringBuilder sb = new StringBuilder();

            // Remember that the array will only have one String
            String url = urls[0];

            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));

                String s = "";

                while ((s = buffer.readLine()) != null) {

                    sb.append(s);

                }

            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String response) {

            readResponse(response);

            displayResponse();

            pDlg.dismiss();

        }

    }

    /*
    * 获取PopupWindow实例
    */
    private void getPopupWindowInstance() {
        if (null != mPopupWindow) {
            mPopupWindow.dismiss();
            return;
        } else {
            initPopuptWindow();
        }
    }

    private void initPopuptWindow()
    {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View popupWindow = layoutInflater.inflate(R.layout.popup_window, null);

        TextView tv_kChart = (TextView)popupWindow.findViewById(R.id.tv_kChart);

        /*
                创建一个PopupWindow
                参数1：contentView 指定PopupWindow的内容
                参数2：width 指定PopupWindow的width
                参数3：height 指定PopupWindow的height
                */
        mPopupWindow = new PopupWindow(popupWindow, 100, 130);
        //these three lines disappear with outside touchable
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        // 获取屏幕和PopupWindow的width和height
        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenWidth = getWindowManager().getDefaultDisplay().getHeight();
        mPopupWindowWidth = mPopupWindow.getWidth();
        mPopupWindowHeight = mPopupWindow.getHeight();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
