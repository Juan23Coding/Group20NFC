package com.example.myapplication;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.util.ArrayList;
import java.util.List;

public class NdefMessageParser {
    private NdefMessageParser() {
    }

    public static List<parsedNdefRecord> parse(NdefMessage message) {
        return getRecords(message.getRecords());
    }

    public static List<parsedNdefRecord> getRecords(NdefRecord[] records) {
        List<parsedNdefRecord> elements = new ArrayList<parsedNdefRecord>();

        for (final NdefRecord record:records) {
            if (UriRecord.isUri(record)) {
                elements.add(UriRecord.parse(record));
            } else if (TextRecord.isText(record)) {
                elements.add(TextRecord.parse(record));
            } else {
                    elements.add(new parsedNdefRecord() {
                        public String str() {
                            return new String(record.getPayload());
                        }
                    });
                }
        }
        return elements;
    }
}
