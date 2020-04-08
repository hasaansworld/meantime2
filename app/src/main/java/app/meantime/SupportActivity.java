package app.meantime;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SupportActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    Toolbar toolbar;
    private BillingClient billingClient;
    MaterialButton buyCoffee;
    SkuDetails coffeeSku;
    TextView textError, headline, text;
    ProgressBar progressBar;
    private final String PURCHASE_ID = "buy_coffee";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        headline = findViewById(R.id.headline);
        text = findViewById(R.id.text);
        textError = findViewById(R.id.textError);
        progressBar = findViewById(R.id.progressBar);

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    buyCoffee.setEnabled(true);
                    queryPurchase();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        buyCoffee = findViewById(R.id.button_buy_coffee);
        buyCoffee.setEnabled(false);
        buyCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textError.setVisibility(View.GONE);
                buyCoffee.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                queryDetails();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("supportUs", false)){
            completedPurchase();
        }
    }

    private void queryDetails(){
        List<String> skuList = new ArrayList<>();
        skuList.add(PURCHASE_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        for (SkuDetails details : skuDetailsList) {
                            if(details.getSku().equals(PURCHASE_ID))
                                startPurchase(details);
                        }
                    }
                    else{
                        textError.setVisibility(View.VISIBLE);
                        textError.setText("Could not load details");
                        buyCoffee.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void startPurchase(SkuDetails details){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(details)
                .build();
        billingClient.launchBillingFlow(SupportActivity.this, flowParams);
    }

    private void completedPurchase(){
        headline.setText("Thank You For Supporting Us.");
        text.setText("We will keep bringing you exciting new features and you will not be shown any ads while you use the app.");
        buyCoffee.setVisibility(View.GONE);
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean("noAds", true);
        editor.putBoolean("supportUs", true);
        editor.apply();
    }

    private void queryPurchase(){
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchaseList = purchasesResult.getPurchasesList();
        if(purchasesResult.getBillingResult().getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseList != null){
            for(Purchase purchase: purchaseList){
                if(purchase.getSku().equals(PURCHASE_ID) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                    completedPurchase();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
            for(Purchase purchase: list){
                if(purchase.getSku().equals(PURCHASE_ID) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                    completedPurchase();
                }
            }
        }
        else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            completedPurchase();
        }
        else{
            textError.setVisibility(View.VISIBLE);
            textError.setText("Failed to complete purchase");
            buyCoffee.setVisibility(View.VISIBLE);
        }
    }

}
