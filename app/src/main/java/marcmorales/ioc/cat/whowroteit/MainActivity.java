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
        //Convertimos el nombre del libro y lo asignamos a una variable con la que realizaremos la consulta
        String queryString = mBookInput.getText().toString();

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        //Verificamos que la conexion de red este disponible
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        //Comprobamos que exista la conexion, que la red este conectada y que exista una cadena de texto para buscar

        if (networkInfo != null && networkInfo.isConnected() && queryString.length() != 0) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);
            /**
             * getSupportLoaderManager() esta deprecated.
             * getSupportLoaderManager().restartLoader(0, queryBundle, this);
             *
             * restartLoader toma tres argumentos.
             *              id: que en caso de que implementasemos mas de un Loader lo usariamos
             *              Bundle: para cualquier dato que necesite el Loader
             *              LoaderCallBacks: instancia lo que implementamos. Si queremos que el Loader
             *              nos proporcione los resultados dentro del MainActivity, usaremos this
             */
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
    //Se llama al crear una instancia del loader
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String queryString = "";

        if (args != null) {
            queryString = args.getString("queryString");
        }
        return new BookLoader(this, queryString);
    }

    @Override
    //Se llama cuando finaliza el loader. Aqui agregamos el codigo para actualizar la interfaz con los datos obtenidos
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        try {
            //Obtenemos los elementos del JSON
            JSONObject jsonObject = new JSONObject(data);
            //recogemos los items del array
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            int i = 0;
            String title = null;
            String authors = null;
            Boolean isAvailable = null;


            while (i < itemsArray.length() && (authors == null && title == null)) {


                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                JSONObject accessInfo = book.getJSONObject("accessInfo");

                try {
                    title = volumeInfo.getString("title");
                    authors = volumeInfo.getString("authors");
                    JSONObject epub = accessInfo.getJSONObject("epub");
                    isAvailable = epub.getBoolean("isAvailable");

                } catch (Exception error) {
                    error.printStackTrace();
                }

                i++;
            }

            String finalTitle = title;
            String finalAuthors = authors;
            Boolean finalIsAvailable = isAvailable;
            switchEpub.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {


                    if (isChecked && finalTitle != null && finalAuthors != null && finalIsAvailable) {
                        mTitleText.setText(finalTitle);
                        mAuthorText.setText(finalAuthors);
                    } else {
                        mTitleText.setText(R.string.no_results);
                        mAuthorText.setText("");
                    }
                }
            });


            if (finalTitle != null && finalAuthors != null && finalIsAvailable) {
                mTitleText.setText(finalTitle);
                mAuthorText.setText(finalAuthors);
            } else {
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
    //No hace nada pero se necesita para la interfaz
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
