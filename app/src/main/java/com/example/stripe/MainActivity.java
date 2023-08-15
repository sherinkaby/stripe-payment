package com.example.stripe;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button button;
    String SECRET_KEY="sk_test_51NXG57KcKj1ieNNwgwREpue3hkj0viYnfp8QEzcflMASKN1uR36sgMhjiG3RynaN6yrqvnC5qUtrI6LLF3t5Zskr00MwzREDzx";
    String PUBLISH_KEY="pk_test_51NXG57KcKj1ieNNwZWbJwwgIzl71cia0vnFez2eJ658IiMh2O9ZNFgpiXlMOUXd6TE9h4qcaeZ9FuU3IEPF725lX00hqnkffxB";
    String customerID;
    String ephericalKey;
    String ClientSecret;
    PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        PaymentConfiguration.init(this,PUBLISH_KEY);

        paymentSheet = new PaymentSheet(this, this::onPaymentresult);

        button=findViewById(R.id.btn);

        button.setOnClickListener(view -> paymentFlow());


        StringRequest stringRequest=new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                response -> {

                    try {
                        JSONObject object=new JSONObject(response);
                        customerID=object.getString("id");
                        Toast.makeText(MainActivity.this,customerID, Toast.LENGTH_SHORT).show();

                        getEphericalKey(customerID);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(MainActivity.this,"error", Toast.LENGTH_SHORT).show()){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String > header= new HashMap<>();
                header.put("Authorization","Bearer"+SECRET_KEY);
                return header;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }

    private void paymentFlow() {

        paymentSheet.presentWithPaymentIntent(ClientSecret,new PaymentSheet.Configuration("Learn with Arvind"
                ,new PaymentSheet.CustomerConfiguration(
                customerID,
                ephericalKey
        )));
    }
    private void onPaymentresult(PaymentSheetResult paymentSheetResult) {

        if (paymentSheetResult instanceof PaymentSheetResult.Completed){
            Toast.makeText(this,"payment success",Toast.LENGTH_SHORT).show();
        }
    }

    private void getEphericalKey(String customerID) {

        StringRequest stringRequest=new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                response -> {

                    try {
                        JSONObject object=new JSONObject(response);
                        ephericalKey=object.getString("id");
                        Toast.makeText(MainActivity.this,ephericalKey, Toast.LENGTH_SHORT).show();

                        getClientSecret(customerID,ephericalKey);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String > header= new HashMap<>();
                header.put("Authorization","Bearer"+SECRET_KEY);
                header.put("Stripe-Version","2022-11-15");
                return header;
            }


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("customer",customerID);
                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }

    private void getClientSecret(String customerID, String ephericalKey) {

        StringRequest stringRequest=new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object=new JSONObject(response);
                            ClientSecret=object.getString("client_secret");
                            Toast.makeText(MainActivity.this,ClientSecret, Toast.LENGTH_SHORT).show();



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String > header= new HashMap<>();
                header.put("Authorization","Bearer"+SECRET_KEY);
                return header;
            }


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("customer",customerID);
                params.put("amount","1000"+"00");
                params.put("currency","usd");
                params.put("automatic_payment_methods[enabled]","true");
                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }



}


