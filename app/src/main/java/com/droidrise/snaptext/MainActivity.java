package com.droidrise.snaptext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.droidrise.snaptext.clipboard.ClipboardService;
import com.droidrise.snaptext.clipboard.TextSnaper;
import com.droidrise.snaptext.model.ClipItem;
import com.droidrise.snaptext.model.ClipsRecyclerViewAdapter;

/**
 * Created by Hai on 4/24/17.
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.recyclerview_mainscreen)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ClipsRecyclerViewAdapter adapter = new ClipsRecyclerViewAdapter(this);
        adapter.setData(ClipboardService.mData);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, OrientationHelper.VERTICAL));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //TODO: Set diver to 6px, need move to dimen
        mRecyclerView.addItemDecoration(new ClipsRecyclerViewAdapter.SpaceItemDecoration(6));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new RecyclerItemTouchHelperCallback(mRecyclerView.getAdapter()));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        // Click event
        adapter.setOnItemClickListener(new ClipsRecyclerViewAdapter.OnRecyclerViewItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {
                ClipItem clip = ClipboardService.mData.get(position);
                TextSnaper snaper = new TextSnaper(view.getContext());
                snaper.showContent(clip.getClip(), clip.getSource());
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            int position = intent.getIntExtra("notify_item_inserted", -1);
            if( position != -1) {
                mRecyclerView.getAdapter().notifyItemInserted(position);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
