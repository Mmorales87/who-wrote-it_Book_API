package marcmorales.ioc.cat.whowroteit;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    // Base URL for Books API.
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    // Parameter for the search string.
    private static final String QUERY_PARAM = "q";
    // Parameter that limits search results.
    private static final String MAX_RESULTS = "maxResults";
    // Parameter to filter by print type.
    private static final String PRINT_TYPE = "printType";
    // Parametro para filtrar por EPUB
    private static final String DOWNLOAD = "download";


    static String getBookInfo(String queryString) {
        //Configuramos las variables
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            /**
             * Creamos la consulta limitando los resultados a 10. Podria hacerse un selector para decidir
             * cuantos resultados como mucho se quiere obtener.
             */
            Uri builtURI = Uri.parse(BOOK_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, queryString + "intitle")
                    .appendQueryParameter(DOWNLOAD, "EPUB").appendQueryParameter(MAX_RESULTS, "10")
                    .appendQueryParameter(PRINT_TYPE, "books").build();

            URL requestURL = new URL(builtURI.toString());

            /**
             * Abrimos conexion
             */
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Configuramos la respuesta
            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            String line;
            //Iteramos leyendo el archivo JSON linea por linea
            while ((line = reader.readLine()) != null){
                builder.append(line);

                builder.append("\n");
            }

            if (builder.length() == 0){
                return null;
            }

            //Convertimos el objeto a String y lo almacenamos en la variable bookJSONString
            bookJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(LOG_TAG, bookJSONString);


        return bookJSONString;
    }
}
