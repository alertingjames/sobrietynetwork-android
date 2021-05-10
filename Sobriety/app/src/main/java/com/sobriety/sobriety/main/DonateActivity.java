package com.sobriety.sobriety.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.config.SkuIds;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.vivek.inapppurchaselib.utils.IabHelper;
import app.vivek.inapppurchaselib.utils.Purchase;
import app.vivek.inapppurchaselib.v3.VKInAppConstants;
import app.vivek.inapppurchaselib.v3.VKInAppProperties;
import app.vivek.inapppurchaselib.v3.VKInAppPurchaseActivity;
import app.vivek.inapppurchaselib.v3.VKLogger;

public class DonateActivity extends AppCompatActivity {

    RadioGroup radioGroup1, radioGroup2;
    RadioButton donate1, donate3, donate5, donate10, donatemore;
    RadioButton[] radioButtons;
    float[] prices = {1.0f, 3.0f, 5.0f, 10.0f};
    LinearLayout priceBox;
    float price = 0.0f;
    String[] productIDs;
    int index = 0;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_donate);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        VKInAppProperties.BASE_64_KEY = "";
        productIDs = new String[]{"111", "222", "333", "444", "555"};

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        radioGroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
        radioGroup2 = (RadioGroup)findViewById(R.id.radioGroup2);

        donate1 = (RadioButton)findViewById(R.id.donate1);
        donate3 = (RadioButton)findViewById(R.id.donate3);
        donate5 = (RadioButton)findViewById(R.id.donate5);
        donate10 = (RadioButton)findViewById(R.id.donate10);
        donatemore = (RadioButton)findViewById(R.id.donatemore);

        priceBox = (LinearLayout)findViewById(R.id.priceBox);

        radioButtons = new RadioButton[]{donate1,donate3, donate5, donate10, donatemore};
        for(int i=0; i<radioButtons.length; i++){
            font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Regular.ttf");
            radioButtons[i].setTypeface(font);
        }

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int ii) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(ii);
                if(radioButton.isChecked()){
                    try{radioGroup2.clearCheck();}catch (NullPointerException e){}
                    for(int i=0; i< radioButtons.length; i++){
                        if(radioButton.getText().equals(radioButtons[i].getText().toString())){
                            index = i;
                            price = prices[index];
                            priceBox.setVisibility(View.GONE);
//                            Toast.makeText(getApplicationContext(), String.valueOf(price), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }
        });

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int ii) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(ii);
                if(radioButton.isChecked()){
                    try{radioGroup1.clearCheck();}catch (NullPointerException e){}
                    for(int i=0; i< radioButtons.length; i++){
                        if(radioButton.getText().equals(radioButtons[i].getText().toString())){
                            index = i; break;
                        }
                    }
                    if(index != 4){
                        price = prices[index];
                        priceBox.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(), String.valueOf(price), Toast.LENGTH_SHORT).show();
                    }else priceBox.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showToast(String content){
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.toast_view, null);
        TextView textView=(TextView)dialogView.findViewById(R.id.text);
        textView.setText(content);
        Toast toast=new Toast(this);
        toast.setView(dialogView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public void donate(View view){
        if(index == 4){
            if(((EditText)findViewById(R.id.priceInputBox)).getText().length() == 0){
                showToast("Enter price");
                return;
            }else price = Float.parseFloat(((EditText)findViewById(R.id.priceInputBox)).getText().toString().trim());
        }
        if(price == 0.0f){
            showToast("Please select price.");
            return;
        }
        payForDonate(productIDs[index]);
    }

    private void payForDonate(String productID){
        Intent intent = new Intent(getApplicationContext(), PayByCardActivity.class);
        intent.putExtra("price", String.valueOf(price));
        startActivity(intent);
//        Intent mIntent=new Intent(DonateActivity.this, VKInAppPurchaseActivity.class);
//        mIntent.putExtra(VKInAppConstants.INAPP_SKU_ID, SkuIds.SKU_INAPP_TEST);
//        mIntent.putExtra(VKInAppConstants.INAPP_SKU_TYPE, IabHelper.ITEM_TYPE_INAPP);
//        mIntent.putExtra(VKInAppConstants.INAPP_PRODUCT_TYPE,VKInAppConstants.INAPP_CONSUMABLE);
//        startActivityForResult(mIntent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==101){
                String mInapSkuId=data.getExtras().getString(VKInAppConstants.INAPP_SKU_ID);
                if(data.getExtras().getString(VKInAppConstants.INAPP_PURCHASE_TOKEN)!=null)
                    VKLogger.e("Purchase Token",data.getExtras().getString(VKInAppConstants.INAPP_PURCHASE_TOKEN));
                Purchase purchaseReceipt;
                if(data.getExtras().containsKey(VKInAppConstants.INAPP_PURCHASE_INFO)){
                    purchaseReceipt= (Purchase) data.getExtras().getSerializable(VKInAppConstants.INAPP_PURCHASE_INFO);
                    VKLogger.e("OrderId:- "+purchaseReceipt.getOrderId()+"\n Token:-"+purchaseReceipt.getToken());
                }

                // {"productId":"appsinvo_day_sub_test","type":"subs","price":"₹ 10.00","price_amount_micros":10000000,"price_currency_code":"INR","title":"OneDaySubscription (InApp Test)","description":"Testing Purpose"}
                int value=data.getExtras().getInt("response_code");
                switch (value) {
                    case VKInAppConstants.RESULT_PRODUCT_CONSUME_SUCCESSFULLY:
                        responseAlertDialog("You have successfully consume "+mInapSkuId+" product.");
                        break;
                    case VKInAppConstants.RESULT_PRODUCT_PURCHASE_CONSUME_SUCCESSFULLY:
                        responseAlertDialog("You have successfully purchase "+mInapSkuId+" product.");
//                        upgradeUser();
                        break;
                    case VKInAppConstants.RESULT_PROPUR_SUCC_CONSUME_FAIL:
                        responseAlertDialog("You have failed to consume "+mInapSkuId+" product.");
                        break;
                    case VKInAppConstants.RESULT_SUBS_CONTINUE:
                        responseAlertDialog("Your subsription is continue for id "+mInapSkuId+" product.");
                        break;
                    case VKInAppConstants.ERROR_DEVICE_NOT_SUPPORT_SUBS:
                        responseAlertDialog(getString(R.string.error_msg_not_support_subs));
                        break;
                    case VKInAppConstants.ERROR_BASE_64_KEY_NOT_SETUP:
                        responseAlertDialog(getString(R.string.error_msg_base64key));
//                        upgradeUser();
                        break;
                    case VKInAppConstants.ERROR_PACKAGE_NAME:
                        responseAlertDialog(getString(R.string.error_msg_package_name));
                        break;
                    case VKInAppConstants.ERROR_DEVICE_NOT_SUPPORT_INAPP:
                        responseAlertDialog(getString(R.string.error_msg_not_support_inapp));
                        break;
                    case VKInAppConstants.ERROR_PRODUCT_PURCHASE:
                        responseAlertDialog(getString(R.string.error_msg_in_purchase));
                        break;

                    default:
                        responseAlertDialog("Error is occured "+value);
                        break;
                }


            }
        }
    }
    /**
     * Show the InApp purchase status dialog
     * @param message
     */
    private void responseAlertDialog(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setCancelable(false);
        bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        bld.create().show();
    }
}





















