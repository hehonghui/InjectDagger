
package org.simple.annotation.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.simple.injector.SimpleDagger;
import org.simple.injector.ViewInjector;

public class MainActivity extends Activity {

    @ViewInjector(R.id.my_tv)
    protected TextView mTextView;

    @ViewInjector(R.id.my_tv2)
    protected TextView mTextView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDagger.inject(this);

        if (mTextView != null) {
            Log.e("", "### my text view : " + mTextView.getText());
        }
    }

}
