package jans.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Bookmark {

    private String action;

    private String user;

    private String name;

    private Date added;

    private URL url;


    public Bookmark() {
    }

    public Bookmark(String user) {
        this.user = user;
    }

    public Bookmark(String user, String name, Date added, String stringUrl, String action) {
        this.user = user;
        this.name = name;
        this.added = added;
        this.action = action;
        try {
            this.url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean equalsURL(Object o) {
        if (this.getUrl()== null) return false;
        if (o == null || getClass() != o.getClass()) return false;
        Bookmark bookmark = (Bookmark) o;
        if (bookmark.getUrl() == null) return false;
        return (bookmark.getUrl().getHost().matches(url.getHost())&&
                bookmark.getUrl().getProtocol().matches(url.getProtocol()));
    }
}
