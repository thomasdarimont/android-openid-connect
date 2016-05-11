package com.lnikkila.oidcsample;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import com.lnikkila.oidcsample.oidc.authenticator.Authenticator;

import java.io.IOException;
import java.util.Map;

/**
 * Initiates the login procedures and contains all UI stuff related to the main activity.
 *
 * @author Leo Nikkilä
 */
public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private Button loginButton;
    private ProgressBar progressBar;

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loginButton = (Button) findViewById(R.id.loginButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        accountManager = AccountManager.get(this);
    }

    /**
     * Called when the user taps the big yellow button.
     */
    public void doLogin(final View view) {
        // Grab all our accounts
        String accountType = getString(R.string.ACCOUNT_TYPE);
        final Account availableAccounts[] = accountManager.getAccountsByType(accountType);

        switch (availableAccounts.length) {
            // No account has been created, let's create one now
            case 0:
                accountManager.addAccount(accountType, Authenticator.TOKEN_TYPE_ID, null, null,
                        this, new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> futureManager) {
                                // Unless the account creation was cancelled, try logging in again
                                // after the account has been created.
                                if (futureManager.isCancelled()) return;
                                doLogin(view);
                            }
                        }, null);
                break;

            // There's just one account, let's use that
            case 1:
                new ApiTask().execute(availableAccounts[0]);
                break;

            // Multiple accounts, let the user pick one
            default:
                String name[] = new String[availableAccounts.length];

                for (int i = 0; i < availableAccounts.length; i++) {
                    name[i] = availableAccounts[i].name;
                }

                new AlertDialog.Builder(this)
                        .setTitle("Choose an account")
                        .setAdapter(new ArrayAdapter<>(this,
                                        android.R.layout.simple_list_item_1, name),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int selectedAccount) {
                                        new ApiTask().execute(availableAccounts[selectedAccount]);
                                    }
                                })
                        .create()
                        .show();
        }
    }

    public void doDeleteAccount(final View view) throws IOException {

        AccountManager accountManager = AccountManager.get(HomeActivity.this);
        String accountType = HomeActivity.this.getString(R.string.ACCOUNT_TYPE);
        Account[] accountsByType = accountManager.getAccountsByType(accountType);

        new DeleteAccountTask().execute(accountsByType);


        loginButton.setText(this.getText(R.string.loginButtonText));
    }

    private class DeleteAccountTask extends AsyncTask<Account, Void, Map> {

        @Override
        protected Map doInBackground(Account... accounts) {


            // Try retrieving an access token from the account manager. The boolean true in the invocation
            // tells Android to show a notification if the token can't be retrieved. When the
            // notification is selected, it will launch the intent for re-authorisation. You could
            // launch it automatically here if you wanted to by grabbing the intent from the bundle.
            try {

                for (Account account : accounts) {

                    CookieManager.getInstance().removeAllCookies(null);

                    //RESET refresh token to force re-authentication
                    accountManager.setAuthToken(account, Authenticator.TOKEN_TYPE_ID, "");
                    accountManager.setAuthToken(account, Authenticator.TOKEN_TYPE_ACCESS, "");
                    accountManager.setAuthToken(account, Authenticator.TOKEN_TYPE_REFRESH, "");

                    accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                        @Override
                        public void run(AccountManagerFuture<Boolean> result) {
                            try {
                                // Get the authenticator result, it is blocking until the
                                // account authenticator completes
                                Log.d(TAG, String.format("Account deleted: %s", result.getResult()));
                            } catch (Exception e) {
                                Log.e(TAG, "Exception during account deletion: ", e);
                            }
                        }
                    }, null);
//                assertTrue("Impossible to delete existing account for this application", removeAccountFuture.getResult(1, TimeUnit.SECONDS));
                }
            } catch (Exception e) {
//             throw new IOException("Could not get ID token from account.", e);
                Log.e(TAG, "Could not delete account", e);
            }

            return null;
        }
    }

    private class ApiTask extends AsyncTask<Account, Void, Map> {

        @Override
        protected void onPreExecute() {
            loginButton.setText("");
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Makes the API request. We could use the OIDCUtils.getUserInfo() method, but we'll do it
         * like this to illustrate making generic API requests after we've logged in.
         */
        @Override
        protected Map doInBackground(Account... args) {
            Account account = args[0];

            try {
                return APIUtility.getJson(HomeActivity.this, Config.userInfoUrl, account);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Processes the API's response.
         */
        @Override
        protected void onPostExecute(Map result) {
            progressBar.setVisibility(View.INVISIBLE);

            if (result == null) {
                loginButton.setText("Couldn't get user info");
            } else {
                loginButton.setText("Logged in as " + result.get("preferred_username"));
            }
        }

    }
}
