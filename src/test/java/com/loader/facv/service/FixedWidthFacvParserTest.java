package com.loader.facv.service;

import com.loader.facv.model.FacvRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FixedWidthFacvParserTest {

    private final FixedWidthFacvParser parser = new FixedWidthFacvParser();

    @Test
    void shouldParseKnownSampleLine() {
        String line = "004065017162001CIF C00000000072 000000    DOD0100000000263";
        FacvRecord record = parser.parse(line);

        Assertions.assertEquals("004", record.getBnkNo());
        Assertions.assertEquals("065017162001", record.getCustAcctNo());
        Assertions.assertEquals("CIF", record.getSysCode());
        Assertions.assertEquals(" ", record.getRecType());
        Assertions.assertEquals("C", record.getCustGp());
        Assertions.assertEquals("00000000072", record.getItlCustNo());
        Assertions.assertEquals(" 000000    ", record.getFiller());
        Assertions.assertEquals("DOD01", record.getLmtId());
        Assertions.assertEquals("00000000263", record.getCustId());
        Assertions.assertEquals("       ", record.getFiller1());
        Assertions.assertEquals(" ", record.getMaintAcct());
    }
}
