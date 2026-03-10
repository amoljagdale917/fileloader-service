package com.loader.facv.service;

import com.loader.facv.model.FacvRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FixedWidthFacvParserTest {

    private final FixedWidthFacvParser parser = new FixedWidthFacvParser();

    @Test
    void shouldParseKnownSampleLineAndSetBlankColumnsToNull() {
        String line = "004065017162001CIF C00000000072 000000    DOD0100000000263";
        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("004", record.getBnkNo());
        Assertions.assertEquals("065017162001", record.getCustAcctNo());
        Assertions.assertEquals("CIF", record.getSysCode());
        Assertions.assertNull(record.getRecType());
        Assertions.assertEquals("C", record.getCustGp());
        Assertions.assertEquals("00000000072", record.getItlCustNo());
        Assertions.assertEquals("000000", record.getFiller());
        Assertions.assertEquals("DOD01", record.getLmtId());
        Assertions.assertEquals("00000000263", record.getCustId());
        Assertions.assertNull(record.getFiller1());
        Assertions.assertNull(record.getMaintAcct());
    }

    @Test
    void shouldParseSecondSampleVariant() {
        String line = "004065017162001DOD C00000000073 000000    DOD0100000000263";
        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("004", record.getBnkNo());
        Assertions.assertEquals("065017162001", record.getCustAcctNo());
        Assertions.assertEquals("DOD", record.getSysCode());
        Assertions.assertNull(record.getRecType());
        Assertions.assertEquals("C", record.getCustGp());
        Assertions.assertEquals("00000000073", record.getItlCustNo());
        Assertions.assertEquals("000000", record.getFiller());
        Assertions.assertEquals("DOD01", record.getLmtId());
        Assertions.assertEquals("00000000263", record.getCustId());
        Assertions.assertNull(record.getFiller1());
        Assertions.assertNull(record.getMaintAcct());
    }

    @Test
    void shouldPadShortLineAndKeepMissingColumnsNull() {
        String line = "004";
        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("004", record.getBnkNo());
        Assertions.assertNull(record.getCustAcctNo());
        Assertions.assertNull(record.getSysCode());
        Assertions.assertNull(record.getRecType());
        Assertions.assertNull(record.getCustGp());
        Assertions.assertNull(record.getItlCustNo());
        Assertions.assertNull(record.getFiller());
        Assertions.assertNull(record.getLmtId());
        Assertions.assertNull(record.getCustId());
        Assertions.assertNull(record.getFiller1());
        Assertions.assertNull(record.getMaintAcct());
    }

    @Test
    void shouldTruncateLongLineToExpectedLength() {
        String baseLine = String.format("%-66s", "004065017162001CIF C00000000072 000000    DOD0100000000263");
        String line = baseLine + "EXTRA_TRAILING_TEXT";
        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("004", record.getBnkNo());
        Assertions.assertEquals("065017162001", record.getCustAcctNo());
        Assertions.assertEquals("CIF", record.getSysCode());
        Assertions.assertNull(record.getRecType());
        Assertions.assertEquals("C", record.getCustGp());
        Assertions.assertEquals("00000000072", record.getItlCustNo());
        Assertions.assertEquals("000000", record.getFiller());
        Assertions.assertEquals("DOD01", record.getLmtId());
        Assertions.assertEquals("00000000263", record.getCustId());
        Assertions.assertNull(record.getFiller1());
        Assertions.assertNull(record.getMaintAcct());
    }

    @Test
    void shouldReturnAllNullWhenInputIsNull() {
        FacvRecord record = parser.parse(null);

        Assertions.assertNull(record.getBnkNo());
        Assertions.assertNull(record.getCustAcctNo());
        Assertions.assertNull(record.getSysCode());
        Assertions.assertNull(record.getRecType());
        Assertions.assertNull(record.getCustGp());
        Assertions.assertNull(record.getItlCustNo());
        Assertions.assertNull(record.getFiller());
        Assertions.assertNull(record.getLmtId());
        Assertions.assertNull(record.getCustId());
        Assertions.assertNull(record.getFiller1());
        Assertions.assertNull(record.getMaintAcct());
    }

    @Test
    void shouldTrimOnlyStartAndEndSpacesAndKeepMiddleSpaces() {
        String line =
                "004" +
                "065017162001" +
                "CIF" +
                " " +
                "C" +
                "00000000072" +
                " AB CD     " +
                "DOD01" +
                "00000000263" +
                "       " +
                " ";
        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("AB CD", record.getFiller());
    }

    @Test
    void shouldTrimAllFieldsAndKeepInnerSpaces() {
        String line =
                " 04" +          // BNK_NO(3)
                "12345678901 " + // CUST_ACCT_NO(12)
                " AB" +          // SYS_COD(3)
                "R" +            // REC_TYPE(1)
                "C" +            // CUST_GP(1)
                " 0000000007" +  // ITL_CUST_NO(11)
                " A B C     " +  // FILLER(11)
                " LM1 " +        // LMT_ID(5)
                " 000000123 " +  // CUST_ID(11)
                "  X Y  " +      // FILLER1(7)
                "M";             // MAINT_ACT(1)

        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("04", record.getBnkNo());
        Assertions.assertEquals("12345678901", record.getCustAcctNo());
        Assertions.assertEquals("AB", record.getSysCode());
        Assertions.assertEquals("R", record.getRecType());
        Assertions.assertEquals("C", record.getCustGp());
        Assertions.assertEquals("0000000007", record.getItlCustNo());
        Assertions.assertEquals("A B C", record.getFiller());
        Assertions.assertEquals("LM1", record.getLmtId());
        Assertions.assertEquals("000000123", record.getCustId());
        Assertions.assertEquals("X Y", record.getFiller1());
        Assertions.assertEquals("M", record.getMaintAcct());
    }
}
