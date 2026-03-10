package com.loader.facv.service;

import com.loader.facv.model.FacvRecord;
import com.loader.facv.util.FixedWidthParserUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FixedWidthFacvParser {

    private static final int TOTAL_RECORD_LENGTH = 66;

    public FacvRecord parse(String line) {
        String normalized = FixedWidthParserUtils.normalize(line, TOTAL_RECORD_LENGTH);
        FacvRecord record = new FacvRecord();
        record.setBnkNo(FixedWidthParserUtils.extract(normalized, 0, 3));
        record.setCustAcctNo(FixedWidthParserUtils.extract(normalized, 3, 15));
        record.setSysCode(FixedWidthParserUtils.extract(normalized, 15, 18));
        record.setRecType(FixedWidthParserUtils.extract(normalized, 18, 19));
        record.setCustGp(FixedWidthParserUtils.extract(normalized, 19, 20));
        record.setItlCustNo(FixedWidthParserUtils.extract(normalized, 20, 31));
        record.setFiller(FixedWidthParserUtils.extract(normalized, 31, 42));
        record.setLmtId(FixedWidthParserUtils.extract(normalized, 42, 47));
        record.setCustId(FixedWidthParserUtils.extract(normalized, 47, 58));
        record.setFiller1(FixedWidthParserUtils.extract(normalized, 58, 65));
        record.setMaintAcct(FixedWidthParserUtils.extract(normalized, 65, 66));
        return record;
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
