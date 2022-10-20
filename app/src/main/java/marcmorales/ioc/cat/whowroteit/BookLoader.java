package marcmorales.ioc.cat.whowroteit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class BookLoader extends AsyncTaskLoader<String> {

    private String mQueryString;



    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        forceLoad();
    }

    public BookLoader(@NonNull Context context, String queryString) {
        super(context);
        mQueryString = queryString;
    }

    @Nullable
    @Override
    public String loadInBackground() {


        return NetworkUtils.getBookInfo(mQueryString);
    }
}
