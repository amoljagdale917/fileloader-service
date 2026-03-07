package com.loader.facv.service;

import com.loader.facv.model.FacvRecord;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FixedWidthFacvParser {

    private static final int TOTAL_RECORD_LENGTH = 66;

    public FacvRecord parse(String line) {
        String normalized = normalize(line);
        FacvRecord record = new FacvRecord();
        record.setBnkNo(extract(normalized, 0, 3));
        record.setCustAcctNo(extract(normalized, 3, 15));
        record.setSysCode(extract(normalized, 15, 18));
        record.setRecType(extract(normalized, 18, 19));
        record.setCustGp(extract(normalized, 19, 20));
        record.setItlCustNo(extract(normalized, 20, 31));
        record.setFiller(extract(normalized, 31, 42));
        record.setLmtId(extract(normalized, 42, 47));
        record.setCustId(extract(normalized, 47, 58));
        record.setFiller1(extract(normalized, 58, 65));
        record.setMaintAcct(extract(normalized, 65, 66));
        return record;
    }

    private String normalize(String line) {
        if (line == null) {
            return repeatSpace(TOTAL_RECORD_LENGTH);
        }

        if (line.length() >= TOTAL_RECORD_LENGTH) {
            return line.substring(0, TOTAL_RECORD_LENGTH);
        }

        StringBuilder sb = new StringBuilder(line);
        while (sb.length() < TOTAL_RECORD_LENGTH) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private String repeatSpace(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ' ';
        }
        return new String(chars);
    }

    private String extract(String source, int startInclusive, int endExclusive) {
        String value = source.substring(startInclusive, endExclusive);
        if (value.trim().isEmpty()) {
            return null;
        }
        return value;
    }

    public List<String> expectedColumnLengths() {
        List<String> lengths = new ArrayList<String>();
        lengths.add("BNK_NO(3)");
        lengths.add("CUST_ACCT_NO(12)");
        lengths.add("SYS_COD(3)");
        lengths.add("REC_TYPE(1)");
        lengths.add("CUST_GP(1)");
        lengths.add("ITL_CUST_NO(11)");
        lengths.add("FILLER(11)");
        lengths.add("LMT_ID(5)");
        lengths.add("CUST_ID(11)");
        lengths.add("FILLER1(7)");
        lengths.add("MAINT_ACT(1)");
        return lengths;
    }
}
