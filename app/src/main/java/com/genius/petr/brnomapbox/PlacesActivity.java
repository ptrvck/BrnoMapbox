package com.genius.petr.brnomapbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PlacesActivity extends FragmentActivity {
    static final int NUM_ITEMS = 3;

    private static Places places;

    MyAdapter mAdapter;

    ViewPager mPager;

    RelativeLayout layout;

    ImageView imageCurrent;
    ImageView imagePrevious;

    Drawable backgroundIcon;


    private void animateIcon(){

/*
        imageCurrent.setTranslationX();
        imageCurrent.animate()
                .translationXBy(-filtersButtonsLayoutWidth)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationEnd(animation);
                        filterButtonsLayout.setVisibility(View.VISIBLE);
                    }
                });
                */
    }

    private void initBackground() {
        imageCurrent = (ImageView) findViewById(R.id.backgroundImage);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        int screenHeight = displaymetrics.heightPixels;

        //image IS recctangular
        int size = Math.min(screenWidth, screenHeight);

        imageCurrent.setLayoutParams(new RelativeLayout.LayoutParams(size, size));

        Context context = getApplicationContext();

        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, R.drawable.database_background);

        //Drawable drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.database_background, context.getTheme());
        imageCurrent.setImageDrawable(drawable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        places = getIntent().getExtras().getParcelable("places");

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        //initBackground();

        layout = (RelativeLayout) findViewById(R.id.layout);

        imageCurrent = (ImageView) findViewById(R.id.backgroundImage);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Context context = getApplicationContext();



                //Drawable drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.database_background, context.getTheme());



                int position = tab.getPosition();
                if (position == 0) {
                    layout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDatabaseAccent));
                    backgroundIcon = AppCompatDrawableManager.get().getDrawable(context, R.drawable.database_background);
                } else if (position == 1) {
                    layout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBarAccent));
                    backgroundIcon = AppCompatDrawableManager.get().getDrawable(context, R.drawable.bar_background);
                } else {
                    layout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRestaurantAccent));
                    backgroundIcon = AppCompatDrawableManager.get().getDrawable(context, R.drawable.restaurant_background);
                }

                Animation anim = new ScaleAnimation(
                        1f, 0f, // Start and end values for the X axis scaling
                        1f, 0f, // Start and end values for the Y axis scaling
                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                        Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                anim.setDuration(200);

                anim.setAnimationListener(new Animation.AnimationListener(){
                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        imageCurrent.setImageDrawable(backgroundIcon);
                        Animation anim = new ScaleAnimation(
                                0f, 1f, // Start and end values for the X axis scaling
                                0f, 1f, // Start and end values for the Y axis scaling
                                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                        anim.setDuration(100);
                        imageCurrent.startAnimation(anim);
                    }
                });
                imageCurrent.startAnimation(anim);

                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        //private ArrayList<ArrayListFragment> fragments;

        public MyAdapter(FragmentManager fm) {
            super(fm);
           /* for (int i = 0; i<NUM_ITEMS; i++)
                fragments.add(ArrayListFragment.newInstance(i));
                */
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return ArrayListFragment.newInstance(places.getDatabases());
            } else if (position == 1) {
                return ArrayListFragment.newInstance(places.getBars());
            } else {
                return ArrayListFragment.newInstance(places.getRestaurants());
            }
            //return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            String title = "Tab "+Integer.toString(position);
            return title;
        }

    }

    public static class ArrayListFragment extends ListFragment {
        String category;
        ArrayList<Place> data;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static ArrayListFragment newInstance(ArrayList<Place> data) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putParcelableArrayList("data", data);
            args.putString("category", "database");
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            data = getArguments() != null ? getArguments().<Place>getParcelableArrayList("data") : null;
            category = getArguments() != null ? getArguments().getString("category") : "unknown";
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_places_list, container, false);
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            super.onActivityCreated(savedInstanceState);
            /*
            ArrayList<Place> items = new ArrayList<>();
            Place p1 = new Place(1);
            p1.setName("Place one");
            p1.setType("Type one");

            Place p2 = new Place(2);
            p2.setName("Place two");
            p2.setType("Type two");

            items.add(p1);
            items.add(p2);
*/
            setListAdapter(new PlaceListAdapter(getActivity(), data));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.i("FragmentList", "Item clicked: " + id);
            Place p = data.get(position);
            if (p != null) {
                Log.i("FragmentList", "Item clicked: " + p.getName());
            }
            Intent openMainActivity= new Intent(getActivity(), MainActivity.class);
            openMainActivity.putExtra("id",p.getId());
            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            startActivity(openMainActivity);
        }
    }
}
