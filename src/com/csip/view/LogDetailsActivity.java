
package com.csip.view;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.csip.view.LogDetailsFragment.OnquitListener;
import com.csipsimple.utils.Compatibility;

public class LogDetailsActivity extends SherlockFragmentActivity implements OnquitListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
           LogDetailsFragment  detailFragment = new LogDetailsFragment();
            detailFragment.setArguments(getIntent().getExtras());
            detailFragment.setOnQuitListener(this);
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, detailFragment).commit();
        }
	}
	
	@Override
	protected void onStart() {
		super.onStart();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == Compatibility.getHomeMenuId()) {
	         finish();
	         return true;
	    }

        return super.onOptionsItemSelected(item);
	}

	@Override
	public void onquit() {
		finish();
	}

	@Override
	public void onShowCallLog(long[] callsId) {
		
	}
}
