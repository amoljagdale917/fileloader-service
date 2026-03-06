package com.loader.facv.repository;

import com.loader.facv.model.FacvRecord;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class FacvRecordRepository {

    private static final String INSERT_SQL =
            "INSERT INTO STG_HK_OBS_FACV (" +
                    "BNK_NO, CUST_ACCT_NO, SYS_CODE, REC_TYPE, CUST_GP, " +
                    "ITL_CUST_NO, FILLER, LMT_ID, CUST_ID, FILLER1, MAINT_ACCT" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(final List<FacvRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FacvRecord record = records.get(i);
                ps.setString(1, record.getBnkNo());
                ps.setString(2, record.getCustAcctNo());
                ps.setString(3, record.getSysCode());
                ps.setString(4, record.getRecType());
                ps.setString(5, record.getCustGp());
                ps.setString(6, record.getItlCustNo());
                ps.setString(7, record.getFiller());
                ps.setString(8, record.getLmtId());
                ps.setString(9, record.getCustId());
                ps.setString(10, record.getFiller1());
                ps.setString(11, record.getMaintAcct());
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }
}
