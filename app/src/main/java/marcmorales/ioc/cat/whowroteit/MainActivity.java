package marcmorales.ioc.cat.whowroteit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;
    private Switch switchEpub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = (EditText) findViewById(R.id.bookInput);
        mTitleText = (TextView) findViewById(R.id.titleText);
        mAuthorText = (TextView) findViewById(R.id.authorText);
        switchEpub = (Switch) findViewById(R.id.swEpub);


        if (LoaderManager.getInstance(this).getLoader(0) != null) {
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }
    }

    public void searchBooks(View view) {

        String queryString = mBookInput.getText().toString();

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }


        if (networkInfo != null && networkInfo.isConnected() && queryString.length() != 0) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);

            LoaderManager.getInstance(this).restartLoader(0, queryBundle, this);

            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);
        } else {
            if (queryString.length() == 0) {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_search_term);
            } else {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_network);
            }
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String queryString = "";

        if (args != null) {
            queryString = args.getString("queryString");
        }
        return new BookLoader(this, queryString);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        System.out.println(data);

        try {

            JSONObject jsonObject = new JSONObject(data);
            JSONArray itemsArray = jsonObject.getJSONArray("items");
            Boolean findAvailable = switchEpub.isChecked();

            for( int index = 0; index < itemsArray.length(); ++index)
            {
                // Accedemos al libro
                JSONObject currentBook = itemsArray.getJSONObject( index );

                JSONObject currentBookVolumeInfo = currentBook.getJSONObject( "volumeInfo" );
                String currentBookTitle = currentBookVolumeInfo.getString("title");
                String currentBookAuthors = currentBookVolumeInfo.getString("authors");

                JSONObject currentBookAccessInfo = currentBook.getJSONObject("accessInfo");
                Boolean currentBookIsAvailable = currentBookAccessInfo.getJSONObject("epub").getBoolean("isAvailable");

                if( currentBookTitle != null && currentBookAuthors != null && currentBookIsAvailable == findAvailable )
                {
                    mTitleText.setText(currentBookTitle);
                    mAuthorText.setText(currentBookAuthors);
                    break;
                }

                // Mientras no se cumple el if de arriba, comprovamos esto
                if( index != itemsArray.length() - 1 ) continue;

                mTitleText.setText(R.string.no_results);
                mAuthorText.setText("");
            }

        } catch (Exception error) {
            mTitleText.setText(R.string.no_results);
            mAuthorText.setText("");
            error.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
