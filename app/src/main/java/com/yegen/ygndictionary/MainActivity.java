package com.yegen.ygndictionary;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {
    protected ProgressDialog pleaseWait;
    private ListView apiListview;
    private Button edtSearchButton;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findviews();
        events();
    }
    private void findviews(){
        apiListview = findViewById(R.id.apiListView);
        edtSearchButton=findViewById(R.id.searchButton);
        edtSearch=findViewById(R.id.searchEditText);
    }
    private void setVisibleApiListView(){
        apiListview.setVisibility(View.GONE);
    }
    private void events(){
        edtSearchButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                if (edtSearch.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Fill in the blank", Toast.LENGTH_SHORT).show();
                    setVisibleApiListView();
                }
                else {performSearch();}
            }
        });

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                  if (i == EditorInfo.IME_ACTION_SEARCH || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                      if (edtSearch.getText().toString().isEmpty())
                      {
                          Toast.makeText(MainActivity.this, "Must be fill the box ", Toast.LENGTH_SHORT).show();
                          setVisibleApiListView();
                      }
                      else
                      {
                          performSearch();
                          return true;
                      }
                  }
                  return false;
            }
        });
    }
    private  void performSearch(){
        String word = edtSearch.getText().toString();
        new DictionaryTask().execute(word);
    }

    private class DictionaryTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                pleaseWait = ProgressDialog.show(MainActivity.this, MainActivity.this.getResources().getString(R.string.loading), MainActivity.this.getResources().getString(R.string.please_wait));
         }
        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(String... params) {
            String word = params[0];
            String apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                inputStream.close();
                urlConnection.disconnect();

                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pleaseWait != null) {
                pleaseWait.dismiss();
            }
            if (result != null) {
                try {
                    ArrayList<Word> words = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonWord = jsonArray.getJSONObject(i);

                        JSONArray meaningsArray = jsonWord.getJSONArray("meanings");
                        for (int j = 0; j < meaningsArray.length(); j++) {
                            JSONObject meaningObject = meaningsArray.getJSONObject(j);

                            JSONArray definitionsArray = meaningObject.getJSONArray("definitions");
                            for (int k = 0; k < definitionsArray.length(); k++) {
                                Word w= new Word();
                                JSONObject definitionObject = definitionsArray.getJSONObject(k);
                                if (definitionObject.isNull("definition") == false) {
                                    w.Meanings=definitionObject.getString("definition");
                                }
                                words.add(w);
                            }
                            WordAdapter adapter = new WordAdapter(MainActivity.this, R.layout.words_adapter, words);
                            apiListview.setAdapter((WordAdapter) adapter);
                            apiListview.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Could not find for "+edtSearch.getText().toString(), Toast.LENGTH_SHORT).show();
                setVisibleApiListView();
            }
        }
    }
}
