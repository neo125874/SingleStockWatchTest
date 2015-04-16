package stock.kuo.com.singlestockwatchtest;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tw4585 on 2015/4/16.
 */
public class RSSFeedAdapter extends ArrayAdapter<RSSFeed>
{
    private int resource;
    private List<RSSFeed> items;

    //constructor
    public RSSFeedAdapter(Context context, int resource, List<RSSFeed> items)
    {
        super(context, resource, items);
        this.resource = resource;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout itemView;
        // 讀取目前位置的記事物件
        final RSSFeed item = getItem(position);

        if (convertView == null) {
            // 建立項目畫面元件
            itemView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater)getContext().getSystemService(inflater);
            li.inflate(resource, itemView, true);
        }
        else {
            itemView = (LinearLayout) convertView;
        }

        // 讀取元件
        final RelativeLayout relativeLayout = (RelativeLayout) itemView.findViewById(R.id.feed_rela);
        LinearLayout linearLayout = (LinearLayout)itemView.findViewById(R.id.feed_line);
        /*final WebView webView = (WebView)itemView.findViewById(R.id.feed_webView);
                    Button button = (Button)itemView.findViewById(R.id.btn_close);*/

        TextView tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        //TextView tv_link = (TextView) itemView.findViewById(R.id.tv_link);
        TextView tv_date = (TextView) itemView.findViewById(R.id.tv_date);

        //Link string by SpannableString
        SpannableString ss = new SpannableString(item.getTitle());
        ss.setSpan(new URLSpan(item.getLink()), 0, item.getTitle().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // 設定
        tv_title.setText(ss);
        tv_title.setMovementMethod(LinkMovementMethod.getInstance());
        //tv_link.setText(item.getLink());
        tv_date.setText(item.getDate());

        return itemView;
    }
}
