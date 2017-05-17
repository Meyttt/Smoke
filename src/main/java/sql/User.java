package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by a.chebotareva on 17.05.2017.
 */
public class User {
    private  String id,operator_id,email,name,login,md5_password, create_date,role,status, snils,phone,data_hash,appointment;
    boolean is_unsubscribed;

    public User(String id, String operator_id, String email, String name, String login, String md5_password, String create_date, String role, String status, String snils, String phone, String data_hash, String appointment, boolean is_unsubscribed) {
        this.id = id;
        this.operator_id = operator_id;
        this.email = email;
        this.name = name;
        this.login = login;
        this.md5_password=md5_password;
        this.create_date = create_date;
        this.role = role;
        this.status = status;
        this.snils = snils;
        this.phone = phone;
        this.data_hash = data_hash;
        this.appointment = appointment;
        this.is_unsubscribed = is_unsubscribed;
    }

    public User(ResultSet resultSet) throws SQLException {
        this.id=resultSet.getBigDecimal("id").toString();
        this.operator_id=resultSet.getBigDecimal("operator_id").toString();
        this.email=resultSet.getString("email");
        this.name=resultSet.getString("name");
        this.login=resultSet.getString("login");
        this.md5_password=resultSet.getString("md5_password");
        this.create_date=resultSet.getTimestamp("create_date").toString();
        this.role=resultSet.getString("role");
        this.status=resultSet.getString("status");
        this.snils=resultSet.getString("snils");
        this.phone=resultSet.getString("phone");
        this.data_hash=resultSet.getString("data_hash");
        this.appointment=resultSet.getString("appointment");
        this.is_unsubscribed=resultSet.getBoolean("is_unsubscribed");
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOperator_id() {
        return operator_id;
    }
};
