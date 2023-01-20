package kalaathon.com.upload;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kalaathon.com.R;
import kalaathon.com.models.Contest;

public class paidFragment extends Fragment {

    private Button proceed;
    private ProgressBar mProgressBar;

    private RadioGroup mGroup;
    private RadioButton paytm,upi,bank;
    private EditText paytm_num,upi_text,account_num,ifsc,name;
    private TextView order_id_view;
    private Button next;
    private int check=0;
    private TextInputLayout paytmtext,upitext;
    private LinearLayout accounttext,hide;
    private NestedScrollView mScrollView;
    private String mode,orderid;
    private RelativeLayout success;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.paid_fragment, container, false);
        proceed=view.findViewById(R.id.paidbtnproceed);
        mProgressBar=view.findViewById(R.id.paymentprogress);
        mProgressBar.setVisibility(View.GONE);
        mGroup=view.findViewById(R.id.radiogrp);
        paytm=view.findViewById(R.id.paytm);
        upi=view.findViewById(R.id.upi);
        bank=view.findViewById(R.id.bank);
        paytm_num=view.findViewById(R.id.paytm_et);
        upi_text=view.findViewById(R.id.upi_et);
        account_num=view.findViewById(R.id.bank_account);
        ifsc=view.findViewById(R.id.bank_ifsc);
        name=view.findViewById(R.id.bank_account_name);
        next=view.findViewById(R.id.paid_next);
        order_id_view=view.findViewById(R.id.order_id);
        paytmtext=view.findViewById(R.id.paytmtext);
        upitext=view.findViewById(R.id.upitext);
        accounttext=view.findViewById(R.id.accounttext);
        mScrollView=view.findViewById(R.id.scroll);
        success=view.findViewById(R.id.paid_success);
        hide=view.findViewById(R.id.paid_hide);

        hide.setVisibility(View.VISIBLE);
        success.setVisibility(View.GONE);

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference.child("contest");

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceed.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (isAdded() && getContext()!=null) {
                                Contest data = dataSnapshot.getValue(Contest.class);
                                if (data.getValue().equals("false")) {
                                    mProgressBar.setVisibility(View.GONE);
                                    proceed.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), "" + data.getText(), Toast.LENGTH_LONG).show();
                                } else if (data.getValue().equals("true")) {
                                    payment();
                                }
                            }
                        } catch (Exception e) {
                            mProgressBar.setVisibility(View.GONE);
                            proceed.setVisibility(View.VISIBLE);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        paytmtext.setVisibility(View.GONE);
        accounttext.setVisibility(View.GONE);
        upitext.setVisibility(View.GONE);

        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radiobtn(radioGroup);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean fill=false;
                if(check>0)
                {
                    if(check==1){
                        mode=paytm_num.getText().toString().trim();
                        if(mode.length()==10)
                            fill=true;
                    }
                    else if(check==2){
                        mode=upi_text.getText().toString().trim();
                        if(mode.length()>2)
                            fill=true;
                    }
                    else if(check==3)
                    {
                        mode=account_num.getText().toString().trim()+"_"+ifsc.getText().toString().trim()+"_"+name.getText().toString().trim();
                        if(!account_num.getText().toString().trim().isEmpty()
                                && !ifsc.getText().toString().trim().isEmpty()
                                && !name.getText().toString().trim().isEmpty())
                            fill=true;
                    }
                    if(fill) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                        reference.child("paid").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("mode").setValue(mode);
                        reference.child("paid").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("order_id").setValue(orderid);
                        Intent i=new Intent(getContext(),shareActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("contest","paid");
                        startActivity(i);
                        getActivity().finish();
                    }
                    else Toast.makeText(getContext(), "Incomplete Information!", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(getContext(), "Select mode in which you want to be paid.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void payment()
    {
        String M_id="YxWYCH74034718355087";
        String customer_id= FirebaseAuth.getInstance().getUid();
        String order_id= UUID.randomUUID().toString().substring(0,28);
        String url="https://kalaathon.com/paytm/generateChecksum.php";
        String callback_url="https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        RequestQueue requestQueue= Volley.newRequestQueue(getContext());

        StringRequest request=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject.has("CHECKSUMHASH")){
                        String CHECKSUMHASH=jsonObject.getString("CHECKSUMHASH");
                        PaytmPGService paytmPGService = PaytmPGService.getProductionService();

                        HashMap<String, String> paramMap = new HashMap<String, String>();
                        paramMap.put("MID", M_id);
                        paramMap.put("ORDER_ID", order_id);
                        paramMap.put("CUST_ID",customer_id);
                        paramMap.put("CHANNEL_ID", "WAP");
                        paramMap.put("TXN_AMOUNT", "25.0");
                        paramMap.put("WEBSITE", "DEFAULT");
                        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                        paramMap.put("CALLBACK_URL",callback_url);
                        paramMap.put("CHECKSUMHASH",CHECKSUMHASH);

                        PaytmOrder order=new PaytmOrder(paramMap);
                        paytmPGService.initialize(order,null);
                        paytmPGService.startPaymentTransaction(getContext(), true, true,
                                new PaytmPaymentTransactionCallback() {
                                    @Override
                                    public void onTransactionResponse(Bundle bundle) {

                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        if(bundle.getString("STATUS").equals("TXN_SUCCESS")){
                                            order_id_view.setText(bundle.getString("ORDERID"));
                                            orderid=bundle.getString("ORDERID");
                                            hide.setVisibility(View.GONE);
                                            success.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    @Override
                                    public void networkNotAvailable() {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), "No network available.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onErrorProceed(String s) {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void clientAuthenticationFailed(String s) {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void someUIErrorOccurred(String s) {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onErrorLoadingWebPage(int i, String s, String s1) {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onBackPressedCancelTransaction() {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), "Transaction cancelled.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onTransactionCancel(String s, Bundle bundle) {
                                        mProgressBar.setVisibility(View.GONE);
                                        proceed.setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), "Transaction cancelled.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("MID", M_id);
                paramMap.put("ORDER_ID", order_id);
                paramMap.put("CUST_ID",customer_id);
                paramMap.put("CHANNEL_ID", "WAP");
                paramMap.put("TXN_AMOUNT", "25.0");
                paramMap.put("WEBSITE", "DEFAULT");
                paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                paramMap.put("CALLBACK_URL",callback_url);
                return paramMap;
            }
        };
        requestQueue.add(request);
    }
    private void radiobtn(RadioGroup radioGroup)
    {
        if (radioGroup.getCheckedRadioButtonId() == -1)
        {
            check=0;
        }
        else
        {
            if(paytm.isChecked())
            {
                check=1;
                paytmtext.setVisibility(View.VISIBLE);
                paytm_num.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(3));
                accounttext.setVisibility(View.GONE);
                upitext.setVisibility(View.GONE);
            }
            else if(upi.isChecked())
            {
                check=2;
                paytmtext.setVisibility(View.GONE);
                upitext.setVisibility(View.VISIBLE);
                accounttext.setVisibility(View.GONE);
            }
            else if(bank.isChecked())
            {
                check=3;
                paytmtext.setVisibility(View.GONE);
                accounttext.setVisibility(View.VISIBLE);
                upitext.setVisibility(View.GONE);
                mScrollView.post(new Runnable() {
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }
    }
}
