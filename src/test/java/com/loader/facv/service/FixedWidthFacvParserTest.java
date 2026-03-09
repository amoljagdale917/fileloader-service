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
        Assertions.assertEquals(" 000000    ", record.getFiller());
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
        Assertions.assertEquals(" 000000    ", record.getFiller());
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
        System.out.println(baseLine);
        String line = baseLine + "EXTRA_TRAILING_TEXT";
        FacvRecord record = parser.parse(line);
        System.out.println(line);

        Assertions.assertEquals("004", record.getBnkNo());
        Assertions.assertEquals("065017162001", record.getCustAcctNo());
        Assertions.assertEquals("CIF", record.getSysCode());
        Assertions.assertNull(record.getRecType());
        Assertions.assertEquals("C", record.getCustGp());
        Assertions.assertEquals("00000000072", record.getItlCustNo());
        Assertions.assertEquals(" 000000    ", record.getFiller());
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
}
