package com.martynaskairys.udacitybl;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button_search)
    Button mSearchButton;
    @BindView(R.id.edit_text_search)
    EditText mSearchEditText;
    @BindView(R.id.info_text_information)
    TextView mInfoTextView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static final String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new BookAdapter(new ArrayList<Book>(), new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {

            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookAsyncTask task = new BookAsyncTask();
                task.execute();
            }
        });
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {

        private String searchInput = mSearchEditText.getText().toString();

        protected ArrayList<Book> doInBackground(URL... urls) {

            if (searchInput.length() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "enter search keyword", Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

            searchInput = searchInput.replace(" ", "+");

            URL url = createUrl(BOOK_REQUEST_URL + searchInput);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {

                Log.e(LOG_TAG, "IOException", e);
            }

            ArrayList<Book> books = extractBookInfoFromJson(jsonResponse);

            return books;

        }

        @Override
        protected void onPostExecute(ArrayList<Book> bookList) {

            if (bookList == null) {
                mAdapter = new BookAdapter(new ArrayList<Book>(), new BookAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book book) {

                    }
                });
                mRecyclerView.setAdapter(mAdapter);
                mInfoTextView.setVisibility(View.VISIBLE);
                return;
            }
            mAdapter = new BookAdapter(bookList, new BookAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Book book) {
                    String url = book.getBookInfoLink();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
            mRecyclerView.setAdapter(mAdapter);
            mInfoTextView.setVisibility(View.GONE);
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Creating URL error", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {

            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Response code error: " + urlConnection.getResponseCode());

                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem getting JSON results", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {

            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }

            return output.toString();
        }

        private ArrayList<Book> extractBookInfoFromJson(String bookJSON) {

            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }

            ArrayList<Book> books = new ArrayList<Book>();

            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                if (baseJsonResponse.getInt("totalItems") == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Results not found", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return null;
                }
                JSONArray itemArray = baseJsonResponse.getJSONArray("items");

                for (int i = 0; i < itemArray.length(); i++) {
                    JSONObject currentItem = itemArray.getJSONObject(i);
                    JSONObject bookInfo = currentItem.getJSONObject("volumeInfo");

                    String title = bookInfo.getString("title");

                    String[] authors = new String[]{};
                    JSONArray authorJsonArray = bookInfo.optJSONArray("authors");
                    if (authorJsonArray != null) {
                        ArrayList<String> authorList = new ArrayList<String>();
                        for (int j = 0; j < authorJsonArray.length(); j++) {
                            authorList.add(authorJsonArray.get(j).toString());
                        }
                        authors = authorList.toArray(new String[authorList.size()]);
                    }

                    String description = "";
                    if (bookInfo.optString("description") != null)
                        description = bookInfo.optString("description");

                    String infoLink = "";
                    if (bookInfo.optString("infoLink") != null)
                        infoLink = bookInfo.optString("infoLink");

                    String image = "";
                    if (bookInfo.optString("image") != null)
                        image = bookInfo.optString("image");

                    books.add(new Book(title, description, infoLink, authors, image));

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem with parsing book JSON results", e);
            }

            return books;

        }


    }
}
