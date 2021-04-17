package me.wizos.loread.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.viewmodel.FeedListViewModel;
import me.wizos.loread.adapter.FeedPagedListAdapter;
import me.wizos.loread.bean.SpinnerData;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.utils.DictUtils;
import me.wizos.loread.view.colorful.Colorful;

/**
 * 内置的 webView 页面，用来相应 a，iframe 的跳转内容
 * @author Wizos
 */
public class FeedManagerActivity extends BaseActivity {
    @BindView(R.id.feed_manager_toolbar)
    Toolbar toolbar;

    @BindView(R.id.feed_manager_category_spinner)
    Spinner categorySpinner;

    @BindView(R.id.feed_manager_order_spinner)
    Spinner orderSpinner;

    @BindView(R.id.feed_manager_order_direction)
    CheckBox orderCheckBox;

    @BindView(R.id.feed_manager_filter_edit)
    EditText filterEdit;

    @BindView(R.id.feed_manager_recycler_view)
    RecyclerView feedRecyclerView;

    FeedPagedListAdapter feedPagedListAdapter;
    FeedListViewModel feedListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_namager);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState != null) {
            onRecoveryInstanceState(savedInstanceState);
        }


        List<SpinnerData> categoriesData = new ArrayList<>();
        SpinnerData rootSpinnerData = new SpinnerData(getString(R.string.all), App.CATEGORY_ALL);
        categoriesData.add(rootSpinnerData);

        SpinnerData unCategorySpinnerData = new SpinnerData(getString(R.string.un_category), App.CATEGORY_UNCATEGORIZED);
        categoriesData.add(unCategorySpinnerData);

        categoriesData.addAll(CoreDB.i().categoryDao().getCategoriesForSpinnerData(App.i().getUser().getId()));

        categorySpinner.setAdapter(new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_dropdown_item, categoriesData));
        orderSpinner.setAdapter( new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_dropdown_item, DictUtils.getSpinnerData(this, R.array.feed_manager_order_title, R.array.feed_manager_order_value)) );

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                feedRecyclerView.scrollToPosition(0);
                feedListViewModel.loadFeeds(
                        App.i().getUser().getId(),
                        ((SpinnerData) categorySpinner.getSelectedItem()).getValue(),
                        filterEdit.getText().toString(),
                        ((SpinnerData) orderSpinner.getSelectedItem()).getValue(),
                        orderCheckBox.isChecked(),
                        FeedManagerActivity.this,
                        new Observer<PagedList<Feed>>() {
                            @Override
                            public void onChanged(PagedList<Feed> feeds) {
                                feedPagedListAdapter.submitList(feeds);
                            }
                        }
                );
            }
        };
        filterEdit.addTextChangedListener(afterTextChangedListener);

        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // https://stackoverflow.com/questions/9476665/how-to-change-spinner-text-size-and-text-color
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                feedRecyclerView.scrollToPosition(0);
                feedListViewModel.loadFeeds(
                        App.i().getUser().getId(),
                        ((SpinnerData) categorySpinner.getSelectedItem()).getValue(),
                        filterEdit.getText().toString(),
                        ((SpinnerData) orderSpinner.getSelectedItem()).getValue(),
                        orderCheckBox.isChecked(),
                        FeedManagerActivity.this,
                        new Observer<PagedList<Feed>>() {
                            @Override
                            public void onChanged(PagedList<Feed> feeds) {
                                feedPagedListAdapter.submitList(feeds);
                            }
                        }
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        categorySpinner.setOnItemSelectedListener(onItemSelectedListener);
        orderSpinner.setOnItemSelectedListener(onItemSelectedListener);

        orderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                feedRecyclerView.scrollToPosition(0);
                feedListViewModel.loadFeeds(
                        App.i().getUser().getId(),
                        ((SpinnerData) categorySpinner.getSelectedItem()).getValue(),
                        filterEdit.getText().toString(),
                        ((SpinnerData) orderSpinner.getSelectedItem()).getValue(),
                        isChecked,
                        FeedManagerActivity.this,
                        new Observer<PagedList<Feed>>() {
                            @Override
                            public void onChanged(PagedList<Feed> feeds) {
                                feedPagedListAdapter.submitList(feeds);
                            }
                        }
                );
            }
        });

        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedPagedListAdapter = new FeedPagedListAdapter(this);
        feedPagedListAdapter.setOnClickListener(new FeedPagedListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, Feed feed) {
                Intent intent = new Intent(FeedManagerActivity.this, FeedActivity.class);
                intent.putExtra(Contract.FEED_ID, feed.getId());
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
            }
        });
        feedRecyclerView.setAdapter(feedPagedListAdapter);
        feedListViewModel = new ViewModelProvider(this).get(FeedListViewModel.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void onRecoveryInstanceState(@NonNull Bundle outState) {
    }


    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    //     getMenuInflater().inflate(R.menu.menu_url_rewrite, menu);
    //     return true;
    // }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit() {
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.feed_manager_root, R.attr.root_view_bg)
                .backgroundColor(R.id.feed_manager_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}
