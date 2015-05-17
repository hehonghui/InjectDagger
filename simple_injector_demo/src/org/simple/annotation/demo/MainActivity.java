
package org.simple.annotation.demo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import org.simple.injector.SimpleDagger;
import org.simple.injector.anno.ViewInjector;

public class MainActivity extends FragmentActivity {

    @ViewInjector(R.id.my_tv)
    protected TextView mTextView;

    @ViewInjector(R.id.my_tv2)
    protected TextView mTextView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().add(R.id.container, new TestFragment())
                .commit();

        // 
        SimpleDagger.inject(this);
        
        if (mTextView != null) {
            Log.e("", "### my text view : " + mTextView.getText());
        }
    }

}
