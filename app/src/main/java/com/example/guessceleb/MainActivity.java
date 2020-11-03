 package com.example.guessceleb;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb;
    int locationOfCorrectAnswer = 0;
    String answers[] = new String[4];

    ImageView imageView;
    Button optionButton1, optionButton2, optionButton3, optionButton4;
    TextView scoreTextView;
     public void CelebChosen(View view) throws ExecutionException, InterruptedException {

         String scores[] = scoreTextView.getText().toString().split("/");
         String userScore = scores[0];
         String total = scores[1];
         if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
             int score = Integer.parseInt(userScore);
             score ++;
             userScore = Integer.toString(score);
             Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_LONG);
         }else{
             Toast.makeText(getApplicationContext(), "Incorrect, Correct Answer was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG);
         }
        int totalScore = Integer.parseInt(total);
         totalScore++;
         total = Integer.toString(totalScore);

         scoreTextView.setText(userScore +"/"+total);

         displayQuiz();
     }

     public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadContent extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)(url.openConnection());
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();
                while( data != -1 ){
                    char currentChar = (char) data;
                    result += currentChar;
                    data = reader.read();
                }
                return result;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreTextView = (TextView)(findViewById(R.id.scoreTextView)) ;
        imageView = (ImageView)( findViewById(R.id.imageView) );
        optionButton1 = (Button)( findViewById(R.id.optionButton1) );
        optionButton2 = (Button)( findViewById(R.id.optionButton2) );
        optionButton3 = (Button)( findViewById(R.id.optionButton3) );
        optionButton4 = (Button)( findViewById(R.id.optionButton4) );

        DownloadContent content = new DownloadContent();
        String result = null;

        try {

            result = content.execute("https://www.thetoptens.com/most-popular-celebrities/").get();

            String splitIt[] = result.split("<h1>Most Popular Celebrities</h1>");
            String splitResult[] = splitIt[1].split("<nav id=\"morelists\">");

            //Images
            Pattern p = Pattern.compile("src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            boolean store = true;
            while( m.find() ){
                if( store ){
                    celebUrls.add(m.group(1));
                    store = false;
                    continue;
                }
                String garbage = m.group(1);
                store = true;
            }


            //Names
            String names[] = splitIt[1].split( "<b>" );
            for( int i = 1; i < names.length; i++ ){
                String name[] = names[i].split("</b>");
                celebNames.add(name[0]);
            }

            displayQuiz();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

     private void displayQuiz() throws ExecutionException, InterruptedException {
         Random random = new Random();
         chosenCeleb = random.nextInt(celebUrls.size());

         ImageDownloader imageDownloader = new ImageDownloader();

         Bitmap celebImage;

         celebImage = imageDownloader.execute(celebUrls.get(chosenCeleb)).get();

         imageView.setImageBitmap(celebImage);

         locationOfCorrectAnswer = random.nextInt(4);
         int incorrectAnswerLocation;

         for( int i = 0; i < 4; i++ ){

             if( i == locationOfCorrectAnswer ){
                 answers[i] = celebNames.get(chosenCeleb);
                 continue;
             }
             incorrectAnswerLocation = random.nextInt( celebUrls.size() );
             while( incorrectAnswerLocation == chosenCeleb ){
                 incorrectAnswerLocation = random.nextInt( celebUrls.size() );
             }

             answers[i] = celebNames.get(incorrectAnswerLocation);

         }

         optionButton1.setText(answers[0]);
         optionButton2.setText(answers[1]);
         optionButton3.setText(answers[2]);
         optionButton4.setText(answers[3]);
     }
 }