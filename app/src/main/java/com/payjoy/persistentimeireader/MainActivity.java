package com.payjoy.persistentimeireader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.payjoy.service.PayJoyAccessManager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    TextView textView = findViewById(R.id.PersistentImei1);
    String persistentImei1 = PayJoyAccessManager.getInstance(this).getPersistedImei(0);
    textView.setText("PersistentImei1: " + persistentImei1);

    String systemImei1 = getSystemImei(0, this);
    textView = findViewById(R.id.SystemImei1);
    textView.setText("SystemImei1: " + systemImei1);

    textView = findViewById(R.id.PersistentImei2);
    String persistentImei2 = PayJoyAccessManager.getInstance(this).getPersistedImei(1);
    textView.setText("PersistentImei2: " + persistentImei2);

    textView = findViewById(R.id.SystemImei2);
    String systemImei2 = getSystemImei(1, this);
    textView.setText("SystemImei2: " + systemImei2);

    textView = findViewById(R.id.message1);
    if (!TextUtils.isEmpty(systemImei1) && TextUtils.isEmpty(persistentImei1)) {
      textView.setText("PersistentImei 未初始化");
      textView.setVisibility(View.VISIBLE);
    } else if (!TextUtils.isEmpty(systemImei1) && !systemImei1.equals(persistentImei1)) {
      textView.setText("PersistentImei和系统IMEI不匹配");
      textView.setVisibility(View.VISIBLE);
    }
    textView = findViewById(R.id.message2);
    if (!TextUtils.isEmpty(systemImei2) && TextUtils.isEmpty(persistentImei2)) {
      textView.setText("PersistentImei 未初始化");
      textView.setVisibility(View.VISIBLE);
    } else if (!TextUtils.isEmpty(systemImei2) && !systemImei2.equals(persistentImei2)) {
      textView.setText("PersistentImei和系统IMEI不匹配");
      textView.setVisibility(View.VISIBLE);
    }
  }

  private String getSystemImei(int slot, Context context) {
    TelephonyManager manager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (slot >= manager.getPhoneCount()) {
      return null;
    }
    if (ContextCompat.checkSelfPermission(context,
        "android.permission.READ_PRIVILEGED_PHONE_STATE") !=
        PackageManager.PERMISSION_GRANTED) {
      return null;
    }
    String imei = null;
    switch (manager.getPhoneType()) {
      case TelephonyManager.PHONE_TYPE_GSM:
        imei =  manager.getImei(slot);
        break;
      case TelephonyManager.PHONE_TYPE_CDMA:
        imei = manager.getMeid(slot);
        break;
    }
    if (TextUtils.isEmpty(imei)) {
      return null;
    }
    if (imei.length() == 4) {
      imei = addCheckDigit(imei);
    }
    return imei;
  }

  // Calculate and add 15th digit (checksum) to IMEI
  private static String addCheckDigit(String imei14) {
    if (imei14.length() != 14) {
      return null;
    }

    if (!imei14.matches("\\d{14}")) {
      return null;
    }

    int checkDigit = (10 - computeLuhnSum(imei14)) % 10;
    return imei14 + checkDigit;
  }

  // Returns a number % 10 using the Luhn algorithm
  private static int computeLuhnSum(String digitString) {
    int sum = 0;
    for (int i = 0; i < digitString.length(); i++) {
      int digit = Character.getNumericValue(digitString.charAt(i));
      if (i % 2 == 1) {
        // double every alternate digit
        digit *= 2;
        if (digit >= 10) {
          digit -= 9;
        }
      }
      sum += digit;
    }
    return (sum % 10);
  }
}