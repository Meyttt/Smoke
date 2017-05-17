package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by a.chebotareva on 17.05.2017.
 */
public class Operator {
    private String id, parent_id,external_code,gvc_code,full_name,short_name,inn,ogrn,okpo,okved,okato,oktmo,created,modified;
    private boolean is_master;
    private String message;
    private boolean affiliates;
    private String region_code,closed;
    private String closed_date,modified_by;

    public Operator(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getBigDecimal("id").toString();
        try {
            this.parent_id = resultSet.getBigDecimal("parent_id").toString();
        }catch (NullPointerException e){
            this.parent_id = resultSet.getString("parent_id");
        }
        this.external_code = resultSet.getString("external_code");
        this.gvc_code =resultSet.getString("gvc_code");
        this.full_name = resultSet.getString("full_name");
        this.short_name = resultSet.getString("short_name");
        this.inn = resultSet.getString("inn");
        this.ogrn = resultSet.getString("ogrn");
        this.okpo = resultSet.getString("okpo");
        this.okved = resultSet.getString("okved");
        this.okato = resultSet.getString("okato");
        this.oktmo = resultSet.getString("oktmo");
        this.created =resultSet.getTimestamp("created").toString();
        this.modified = resultSet.getTimestamp("modified").toString();
        this.is_master = resultSet.getBoolean("is_master");
        this.message = resultSet.getString("message");
        this.affiliates = resultSet.getBoolean("affiliates");
        this.region_code = resultSet.getString("region_code");
        this.closed = resultSet.getString("closed");
        this.closed_date = resultSet.getString("closed_date");
        this.modified_by = resultSet.getString("modified_by");
    }

    public String getShort_name() {
        return short_name;
    }
}
