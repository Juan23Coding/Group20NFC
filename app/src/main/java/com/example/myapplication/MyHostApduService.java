package com.example.myapplication;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

public class MyHostApduService extends HostApduService {
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        return new byte[0];
    }

    @Override
    public void onDeactivated(int reason) {

    }
}
