package hr.fer.ftodoric.notifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
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
    private NotificationManagerCompat notificationManager;
    private TextView src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = NotificationManagerCompat.from(this);

        src = (TextView) findViewById(R.id.webView);
        src.setMovementMethod(new ScrollingMovementMethod());

        /**
         * Function fetch() is executing the entire process of connecting to specific website,
         * searching for data in raw html text and listing the items and it's information
         * on the display.
         * Items are regularly updated.
         * Update time is set to 1 hour.
         *
         * @author ftodoric
         */
        fetch();
    }

    public void fetch(){
        String rawHTML;
        Fetcher f = new Fetcher();
        f.execute("https://www.njuskalo.hr/index.php?ctl=search_ads&keywords=commodore&categoryId=9586");
        ArrayList<Item> listOfItems = new ArrayList<>();
        try{
            rawHTML = f.get();
            //Document doc = Jsoup.parse(rawHTML);
            //String pretty = doc.body().text();

            //Searching for ID number and Title of Njuskalo post
            String patternString = "<h3 class=\\\"entity-title\\\"><a name=\\\"([0-9]+)\\\" class=\\\"link\\\" href=\\\"[^\\\"]+\\\">[^<]+</a></h3>";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(rawHTML);

            String currentGroup = "";
            String id;
            String title;
            int bracketCounter;
            while(matcher.find()){
                id = "";
                title = "";
                bracketCounter = 0;
                currentGroup = matcher.group(); //matcher.group() - lexical group found based on given pattern (regex)

                //id searching
                int indexOfIdNumber = 34;
                while(currentGroup.charAt(indexOfIdNumber) != '"'){
                    id += currentGroup.charAt(indexOfIdNumber);
                    ++indexOfIdNumber;
                }

                //title searching
                for(int i = 0; i < currentGroup.length(); ++i){
                    if(currentGroup.charAt(i) == '>') bracketCounter++;
                    if(bracketCounter == 2){
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

            //Searching for the price of each post
            patternString = "([0-9.]+)&nbsp;<span class=\\\"currency\\\">kn</span>";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(rawHTML);

            currentGroup = "";
            String price;
            while(matcher.find()){
                price = "";
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

            //Searching for the date of publishing of each post
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

            //Building the output text for the app textView
            StringBuilder finalText = new StringBuilder();
            for(Item item : listOfItems){
                finalText.append(item.toString() + "\n");
            }
            src.setText(finalText.toString());

            //Notification to the user that the data from njuskalo posts has been updated
            sendOnChannel1(finalText.toString());

        } catch(Exception e) {
            e.printStackTrace();
        }

        //SLEEP TIME = 1h
        refresh(1*3600*1000);
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

    class Fetcher extends AsyncTask<String, Void, String> {
        public String doInBackground(String... urls){
            String result = "";

            //Creating http connection
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

    /**
     * Function that can be called to issue a notification
     *
     * @param text: The text that is printed in expanded notification window.
     */
    public void sendOnChannel1(String text){
        Notification notification = new NotificationCompat.Builder(this, NotificationClass.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_info)
                .setContentTitle("Ažuriranje obavljeno!")
                .setContentText("Prikaži više...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .build();
        notificationManager.notify(1, notification);
    }
}
