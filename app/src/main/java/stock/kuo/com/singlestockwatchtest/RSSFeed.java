package stock.kuo.com.singlestockwatchtest;

import java.util.HashMap;

/**
 * Created by tw4585 on 2015/4/16.
 */
public class RSSFeed
{
    private String title, link, date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public RSSFeed()
    {

    }
    public RSSFeed(HashMap<String, Object> post)
    {
        this.title = post.get("title").toString();
        this.link = post.get("link").toString();
        this.date = post.get("pubDate").toString();
    }
}
