package com.example.pixelarcade.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

public class BuyCoinsActivity extends AppCompatActivity {

    private TextView tvCoinBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy_coins);

        tvCoinBalance = findViewById(R.id.tvCoinBalance);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        updateBalance();

        setupBundle(R.id.bundle500, "500", "+ 0 BONUS", "₹99", 500);
        setupBundle(R.id.bundle1200, "1200", "+ 200 BONUS", "₹199", 1400);
        setupBundle(R.id.bundle2500, "2500", "+ 500 BONUS", "₹399", 3000);
        setupBundle(R.id.bundle5500, "5500", "+ 1500 BONUS", "₹799", 7000);
        setupBundle(R.id.bundle12000, "12000", "+ 4000 BONUS", "₹1599", 16000);
    }

    private void setupBundle(int includeId, String amount, String bonus, String price, int totalCoins) {
        View bundleView = findViewById(includeId);
        TextView tvAmount = bundleView.findViewById(R.id.tvBundleAmount);
        TextView tvBonus = bundleView.findViewById(R.id.tvBundleBonus);
        AppCompatButton btnPurchase = bundleView.findViewById(R.id.btnPurchase);

        tvAmount.setText(amount);
        tvBonus.setText(bonus);
        btnPurchase.setText(price);

        btnPurchase.setOnClickListener(v -> purchaseCoins(totalCoins));
    }

    private void purchaseCoins(int amount) {
        UserDataManager udm = UserDataManager.getInstance(this);
        int currentCoins = udm.getInt("coins", 0);
        int totalEarned = udm.getInt("total_coins_earned", 0);
        
        udm.putInt("coins", currentCoins + amount);
        udm.putInt("total_coins_earned", totalEarned + amount);
        
        // Push to cloud immediately
        udm.pushAllToCloud();
        
        Toast.makeText(this, "PURCHASE SUCCESSFUL! +" + amount + " COINS", Toast.LENGTH_SHORT).show();
        updateBalance();
    }

    private void updateBalance() {
        int coins = UserDataManager.getInstance(this).getInt("coins", 0);
        tvCoinBalance.setText(String.valueOf(coins));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBalance();
    }
}
