package com.example.myapplication;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Charsets;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Preconditions;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableMap;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class SmartPoster implements parsedNdefRecord {
    private final TextRecord mTitleRecord;
    private final UriRecord mUriRecord;
    private final RecommendedAction mAction;
    private final String mType;

    public SmartPoster(TextRecord title, UriRecord uri, RecommendedAction action, String type) {
        mUriRecord = Preconditions.checkNotNull(uri);
        mTitleRecord = title;
        mAction = Preconditions.checkNotNull(action);
        mType = type;
    }

    public UriRecord getUriRecord() {
        return mUriRecord;
    }

    public TextRecord getTitle() {
        return mTitleRecord;
    }

    public static SmartPoster parse(NdefRecord record) {
        Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_SMART_POSTER));
        try {
            NdefMessage subRecords = new NdefMessage(record.getPayload());
            return parse(subRecords.getRecords());
        } catch (FormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static SmartPoster parse(NdefRecord[] recordsRaw) {
        try {
            Iterable<parsedNdefRecord> records = NdefMessageParser.getRecords(recordsRaw);
            UriRecord uri = Iterables.getOnlyElement(Iterables.filter(records, UriRecord.class));
            TextRecord title = getFirstIfExists(records, TextRecord.class);
            RecommendedAction action = parseRecommendation(recordsRaw);
            String type = parseType(recordsRaw);
            return new SmartPoster(title, uri, action, type);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isPoster(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String str() {
        if (mTitleRecord != null) {
            return mTitleRecord.str() + "\n" + mUriRecord.str();
        } else {
            return mUriRecord.str();
        }
    }

    private static <T> T getFirstIfExists(Iterable<?> elements, Class<T> type) {
        Iterable<T> filtered = Iterables.filter(elements, type);
        T instance = null;
        if (!Iterables.isEmpty(filtered)) {
            instance = Iterables.get(filtered, 0);
        }
        return instance;
    }

    public enum RecommendedAction {
        UNKNOWN((byte) -1), DO_ACTION((byte) 0), SAVE_FOR_LATER((byte) 1), OPEN_FOR_EDITING((byte) 2);


        private static final ImmutableMap<Byte, RecommendedAction> LOOKUP;

        static {
            ImmutableMap.Builder<Byte, RecommendedAction> builder = ImmutableMap.builder();
            for (RecommendedAction action : RecommendedAction.values()) {
                builder.put(action.getByte(), action);
            }
            LOOKUP = builder.build();
        }


        private final byte mAction;

        private RecommendedAction(byte val) {
            this.mAction = val;
        }

        private byte getByte() {
            return mAction;
        }
    }

    private static NdefRecord getByType(byte[] type, NdefRecord[] records) {
        for (NdefRecord record:records) {
            if (Arrays.equals(type, record.getType())) {
                return record;
            }
        }
        return null;
    }

    private static final byte[] ACTION_RECORD_TYPE = new byte[] {'a', 'b', 'c'};

    private static RecommendedAction parseRecommendation(NdefRecord[] records) {
        NdefRecord record = getByType(ACTION_RECORD_TYPE, records);
        if (record == null) {
            return RecommendedAction.UNKNOWN;
        }
        byte action = record.getPayload()[0];
        if (RecommendedAction.LOOKUP.containsKey(action)) {
            return RecommendedAction.LOOKUP.get(action);
        }
        return RecommendedAction.UNKNOWN;
    }

    private static final byte[] TYPE_TYPE = new byte[] {'t'};

    private static String parseType(NdefRecord[] records) {
        NdefRecord type = getByType(TYPE_TYPE, records);
        if (type == null) {
            return null;
        }
        return new String(type.getPayload(), Charsets.UTF_8);
    }
}
