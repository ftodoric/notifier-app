package hr.fer.ftodoric.notifier;

import android.widget.TextView;

public class Item {
    private String id;
    private String title;
    private String price;
    private String pubDate;

    public Item(String id, String title, String price, String pubDate){
        this.id = id;
        this.title = title;
        this.price = price;
        this.pubDate = pubDate;
    }

    public String getId(){
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getPrice(){
        return this.price;
    }

    public void setPrice(String price){
        this.price = price;
    }

    public String getPubDate(){
        return this.pubDate;
    }

    public void setPubDate(String date){
        this.pubDate = date;
    }

    @Override
    public String toString(){
        if(this.pubDate != "") {
            StringBuilder sb = new StringBuilder();
            sb.append(this.title + "\n");
            sb.append("\t\t\tPrice: " + this.price + "kn" + "\n");
            sb.append("\t\t\tDate published: " + this.pubDate + "\n");

            return sb.toString();
        } else {
            return "";
        }
    }
}
