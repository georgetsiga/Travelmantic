package com.georgetsiga.travelmantic.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.firebase.ui.auth.AuthUI;
import com.georgetsiga.travelmantic.R;
import com.georgetsiga.travelmantic.adapters.DealsAdapter;
import com.georgetsiga.travelmantic.utils.FireBaseUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.w3c.dom.Text;

public class UserActivity extends AppCompatActivity {
@BindView(R.id.fab) FloatingActionButton fab;
@BindView(R.id.name) TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.fab)
    public void addNewDeal(){
        fab.setOnClickListener((view) -> startActivity(new Intent(this,AdminActivity.class)));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            Log.d("Logout", "User Logged Out");
                            FireBaseUtil.attachListener();
                        });
                FireBaseUtil.detachListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FireBaseUtil.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FireBaseUtil.openFbReference(getString(R.string.firebase_database_name), this);
        hideShowFab();
        RecyclerView rvDeals = findViewById(R.id.rv_list_places);
        final DealsAdapter adapter = new DealsAdapter(this);
        rvDeals.setAdapter(adapter);
        LinearLayoutManager dealsLayoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvDeals.setLayoutManager(dealsLayoutManager);
        FireBaseUtil.attachListener();
    }

    public static Intent newInstance(Context context){
        return new Intent(context,UserActivity.class);
    }

    public void showMenu() {
        hideShowFab();
        invalidateOptionsMenu();
    }

    public void setName(String username){
        name.setText(username);
    }

    public void hideShowFab(){
        if (FireBaseUtil.isAdmin) {
            fab.setVisibility(View.VISIBLE);
        }
        else {
            fab.setVisibility(View.GONE);
        }
    }
}
