package hr.fer.ftodoric.notifier;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {
    private TextView src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        src = (TextView) findViewById(R.id.webView);
        src.setMovementMethod(new ScrollingMovementMethod());

        fetch();
    }

    private void refresh(int ms){
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable(){
            @Override
            public void run() {
                fetch();
            }
        };
        handler.postDelayed(runnable, ms);
    }

    public void fetch(){
        String rawHTML;
        Fetcher f = new Fetcher();
        f.execute("https://www.njuskalo.hr/index.php?ctl=search_ads&keywords=commodore&categoryId=9586");
        ArrayList<Item> listOfItems = new ArrayList<>();
        try{
            rawHTML = f.get();
            Document doc = Jsoup.parse(rawHTML);
            String pretty = doc.body().text();

            //Searching for ID number and Title of njuskalo post
            String patternString = "<h3 class=\\\"entity-title\\\"><a name=\\\"([0-9]+)\\\" class=\\\"link\\\" href=\\\"[^\\\"]+\\\">[^<]+</a></h3>";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(rawHTML);

            String currentGroup = "";
            while(matcher.find()){
                String id = "";
                String title = "";
                int counter = 0;
                currentGroup = matcher.group();
                int index = 34;
                while(currentGroup.charAt(index) != '"'){
                    id += currentGroup.charAt(index);
                    ++index;
                }
                for(int i = 0; i < currentGroup.length(); ++i){
                    if(currentGroup.charAt(i) == '>') counter++;
                    if(counter == 2){
                        int k = 1;
                        while(currentGroup.charAt(i + k) != '<'){
                            title += currentGroup.charAt(i + k);
                            ++k;
                        }
                        break;
                    }
                }
                Item item = new Item(id, title, "", "");
                listOfItems.add(item);
            }

            //Searching for price of each post
            patternString = "([0-9.]+)&nbsp;<span class=\\\"currency\\\">kn</span>";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(rawHTML);

            currentGroup = "";
            while(matcher.find()){
                String price = "";
                currentGroup = matcher.group();
                for(int i = 0; currentGroup.charAt(i) != '&'; ++i){
                    price += currentGroup.charAt(i);
                }
                for(Item item : listOfItems){
                    if(item.getPrice() == ""){
                        item.setPrice(price);
                        break;
                    }
                }
            }

            //Searching for publishing date of each post
            patternString = "pubdate=\\\"pubdate\\\">[^<]*";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(rawHTML);

            currentGroup = "";
            String pubDate;
            while(matcher.find()){
                pubDate = "";
                currentGroup = matcher.group();
                pubDate = currentGroup.substring(18, 18 + 11);
                for(Item item : listOfItems){
                    if(item.getPubDate() == ""){
                        item.setPubDate(pubDate);
                        break;
                    }
                }
            }

            //Building output text for app text view
            StringBuilder finalText = new StringBuilder();
            for(Item item : listOfItems){
                finalText.append(item.toString() + "\n");
            }
            src.setText(finalText.toString());
        } catch(Exception e) {
            e.printStackTrace();
        }

        //SLEEP IN MILLISECONDS: SLEEP TIME = 1h
        refresh(1*3600*1000);
    }

    class Fetcher extends AsyncTask<String, Void, String> {
        public String doInBackground(String... urls){
            String result = "";

            try{
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while((line = reader.readLine()) != null){
                    result = result + line;
                }
                conn.disconnect();

            } catch(IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }
}
