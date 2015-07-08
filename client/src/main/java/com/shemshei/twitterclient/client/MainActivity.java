package com.shemshei.twitterclient.client;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /* Shared preference keys */
        static String TWITTER_CONSUMER_KEY = "LsCQaPOwd8k7WkyRFRZF4Q";
        static String TWITTER_CONSUMER_SECRET = "KJbJu5IQrlwxW7Cwnax3mMzAc4j3n6Wd2dG125srgk";
        static final String TWITTER_CALLBACK_URL = "https://api.twitter.com/oauth";

        // Preference Constants
        static String PREFERENCE_NAME = "twitter_oauth";
        static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
        static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
        static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

        // Twitter oauth urls
        static final String URL_TWITTER_AUTH = "authorize";
        static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
        static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

        // Twitter
        private static Twitter twitter;
        private static RequestToken requestToken;

        // Shared Preferences
        private static SharedPreferences mSharedPreferences;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);

            TextView textView = (TextView) rootView.findViewById(R.id.textview);
            if (isConnectingToInternet()) {
                if (!isTwitterLoggedInAlready()) {
                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                    builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
                    Configuration configuration = builder.build();

                    TwitterFactory factory = new TwitterFactory(configuration);
                    twitter = factory.getInstance();
                    Uri uri = null;
                    try {
                        requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                        uri = Uri.parse(requestToken.getAuthenticationURL());
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }

                    if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                        // oAuth verifier
                        String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                        try {
                            // Get the access token
                            textView.setText("Nothing");
                            if (twitter == null){
                                textView.setText("Twitter NULL");
                            } else if (requestToken == null){
                                textView.setText("requestToken NULL");
                            } else if (verifier == null){
                                textView.setText("verifier NULL");
                            }
                            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                            textView.setText("accessToken");
//                            // Shared Preferences
//                            SharedPreferences.Editor e = mSharedPreferences.edit();
//                            // After getting access token, access token secret
//                            // store them in application preferences
//                            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
//                            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
//                            // Store login status - true
//                            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
//                            e.commit(); // save changes
//
//                            Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
//
//
//                            // Getting user details from twitter
//                            // For now i am getting his name only
                            long userID = accessToken.getUserId();
                            textView.setText("userID");

                            User user = twitter.showUser(userID);
                            textView.setText("user");

                            String username = user.getName();
                            textView.setText("Welcome " + username);
                        } catch (Exception e) {
                            // Check log for login errors
                            Log.e("Twitter Login Error", "> " + e.getMessage());
                        }
                    } else {
                        textView.setText(uri == null ? "uri NULL" : "Uri = " + uri.toString());
                    }
                } else {
                    textView.setText("Twitter is Logged In Already");
                }
            } else {
                textView.setText("There is no internet connection");
            }
            return rootView;
        }

        /**
         * Check user already logged in your application using twitter Login flag is
         * fetched from Shared Preferences
         */
        private boolean isTwitterLoggedInAlready() {
            // return twitter login status from Shared Preferences
            return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
        }

        private boolean isConnectingToInternet() {
            ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
