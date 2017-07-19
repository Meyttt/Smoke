package sql;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import  org.postgresql.Driver;

/**
 * Created by svkreml on 04.11.2016.
 */
public class SqlManager {
    String url;
    String name;
    String password;
    boolean debug = true;
    Connection connection = null;

    public SqlManager(String url, String name, String password) {
        this.url = url;
        this.name = name;
        this.password = password;

        try {
            //todo: спросить, зачем этот вызов
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, name, password);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void DELETE(String table, String column, String condition) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String request = "DELETE FROM " + table + " WHERE " + column + " " + condition;
            if (debug) System.out.println("request = " + request);
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void INSERT(String request) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            if (debug) System.out.println("request = " + request);
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void REQUEST(String request) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            if (debug) System.out.println("request = " + request);
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void UPDATE(String request) {
        Statement statement = null;
        // System.out.println("request = " + request);
        try {
            statement = connection.createStatement();
            if (debug) System.out.println("request = " + request);
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet SELECT(String request) {
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            if (debug) System.out.println("request = " + request);
            resultSet = statement.executeQuery(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LinkedList<HashMap<String, String>> selected = new LinkedList<HashMap<String, String>>();

        return resultSet;
    }

    public ResultSet SELECT(String table, String column, String condition) {
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String request = "SELECT " + column + " FROM " + table + " WHERE " + condition;
            if (debug) System.out.println("request = " + request);
            resultSet = statement.executeQuery("SELECT " + column + " FROM " + table + " WHERE " + condition);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }


    static public String ConvertToSqlTimeStamp(java.util.Date date) {
        SimpleDateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSX");
        return mdyFormat.format(date);
    }


    static public String insertArray(String table, Vector<String> req) {
        StringBuilder output = new StringBuilder("INSERT INTO " + table + " VALUES ");
        for (String line :
                req) {
            output.append(line + ',');
        }
        output.deleteCharAt(output.length() - 1);
        output.append(";");
        return output.toString();
    }

    static public String ArrayBrackets(Vector<String> req) {
        StringBuilder output = new StringBuilder("(");
        for (String line :
                req) {
            output.append(line + ',');
        }
        if (output.length() > 1)
            output.deleteCharAt(output.length() - 1);
        output.append(")");
        return output.toString();
    }
//from there

    public LinkedList<User> selectUsersByEmail(String email) {
        Statement statement = null;
        ResultSet resultSet = null;
        LinkedList<User> users = new LinkedList<User>();
        String request = "SELECT * FROM t_user WHERE email='" + email + "';";
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(request);
            while (resultSet.next()) {
                users.add(new User(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public LinkedList<Operator> selectOperatorById(String id) {
        Statement statement = null;
        ResultSet resultSet = null;
        String request = "SELECT * FROM t_operator WHERE id='" + id + "';";
        LinkedList<Operator> operators = new LinkedList<Operator>();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(request);
            while (resultSet.next()) {
                operators.add(new Operator(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operators;
    }

    public LinkedList<Operator> selectOperatorByParentId(String id) {
        Statement statement = null;
        ResultSet resultSet = null;
        String request = "SELECT * FROM t_operator WHERE parent_id='" + id + "';";
        LinkedList<Operator> operators = new LinkedList<Operator>();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(request);
            while (resultSet.next()) {
                operators.add(new Operator(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operators;
    }
//    public void changeUser(sql.User user){
//        Statement statement=null;
//        String request = "UPDATE t_user SET email='"+user.getEmail()+" "+selectOperatorById(user.getOperator_id()).get(0).getShort_name()+
//                "', login='"+ user.getName()+" "+selectOperatorById(user.getOperator_id()).get(0).getShort_name()+"' WHERE id=" +user.getId()+";";
//    }

    public void changeUser(User user) {
        Statement statement = null;
        java.util.Date date = new java.util.Date();
        String request = "UPDATE t_user SET email='" + user.getEmail() + " " + date +
                "', login='" + user.getName() + " " + date + "' WHERE id=" + user.getId() + ";";
        try {
            statement = connection.createStatement();
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeUser(String email) {
        LinkedList<User> users = this.selectUsersByEmail(email);
        for (User user : users) {
            Statement statement = null;
            java.util.Date date = new java.util.Date();
            String request = "UPDATE t_user SET email='" + user.getEmail() + " " + date +
                    "', login='" + user.getName() + " " + date + "' WHERE id=" + user.getId() + ";";
            try {
                statement = connection.createStatement();
                statement.executeUpdate(request);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void deleteUser(String email) {
        LinkedList<User> users = this.selectUsersByEmail(email);
        for (User user : users) {
            Statement statement = null;
            java.util.Date date = new java.util.Date();
            String request = "CREATE OR REPLACE FUNCTION USERS_DELETE(t_operator_id INTEGER)\n" +
                    "  RETURNS boolean AS\n" +
                    "$BODY$\n" +
                    "DECLARE\n" +
                    "  declare res boolean;\n" +
                    "  declare t_user_id INTEGER;\n" +
                    "BEGIN\n" +
                    "\n" +
                    "FOR t_user_id IN select id from t_user where operator_id = t_operator_id LOOP\n" +
                    "\n" +
                    "delete from t_user_territory where user_id = t_user_id;\n" +
                    "delete from t_password_change_links where user_id = t_user_id;\n" +
                    "delete from t_user where id = t_user_id;\n" +
                    "\n" +
                    "END LOOP;\n" +
                    "\n" +
                    "RETURN res;\n" +
                    "\n" +
                    "END;\n" +
                    "\n" +
                    "$BODY$\n" +
                    "\n" +
                    "LANGUAGE 'plpgsql' VOLATILE;\n" +
                    "\n" +
                    "DO $$ \n" +
                    "BEGIN\n" +
                    "perform USERS_DELETE("+user.getOperator_id()+");\n" +
                    "\n" +
                    "END $$;  ";
            try {
                statement = connection.createStatement();
                statement.executeUpdate(request);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteOperatorHistory(String email) {
        LinkedList<User> users = this.selectUsersByEmail(email);
        for (User user : users) {
            Statement statement = null;
            String request = "update t_report_form_var set status = 'DELETED' where operator_id in (select operator_id from t_user where id= " + user.getId() + ")" +
                    " and status != 'DELETED'";
            try {
                statement = connection.createStatement();
                statement.execute(request);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Statement statement2 = null;
            String request2 = "update t_report_form_var set status = 'DELETED' where operator_id in ( SELECT id FROM t_operator where parent_id in ( select operator_id from t_user where id= " + user.getId() + "))" +
                    " and status != 'DELETED'";
            try {
                statement2 = connection.createStatement();
                statement2.execute(request2);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private void deleteFinally(String operatorId) {
        Statement statement = null;
        String request = "CREATE OR REPLACE FUNCTION FORMS_DELETE()\n" +
                "  RETURNS boolean AS\n" +
                "$BODY$\n" +
                "DECLARE\n" +
                "  declare res boolean;\n" +
                "  declare t_operator_id INTEGER :=" + operatorId + ";\n" +
                "  declare t_form_id INTEGER;\n" +
                "BEGIN\n" +
                "\n" +
                "FOR t_form_id IN select id from t_report_form_var where operator_id = t_operator_id LOOP\n" +
                "\n" +
                "delete from t_incoming_report_log where report_form_var_id = t_form_id;\n" +
                "delete from t_report_chat_history where form_var_id = t_form_id;\n" +
                "delete from t_report_history where report_form_var_id = t_form_id;\n" +
                "delete from t_operator_contact where report_form_var_id = t_form_id;\n" +
                "delete from t_cell_value where form_var_id = t_form_id;\n" +
                "delete from t_titul_data where report_form_var_id = t_form_id;\n" +
                "delete from t_form_var_status_log where form_var_id=t_form_id;\n" +
                "delete from t_form_warning where var_form_id = t_form_id;\n" +
                "delete from t_temp_lob where form_id = t_form_id;\n" +
                "delete from t_report_form_var where id = t_form_id;\n" +
                "\n" +
                "END LOOP;\n" +
                "\n" +
                "RETURN res;\n" +
                "\n" +
                "END;\n" +
                "\n" +
                "$BODY$\n" +
                "\n" +
                "LANGUAGE 'plpgsql' VOLATILE;\n" +
                "\n" +
                "DO $$ \n" +
                "BEGIN\n" +
                "perform FORMS_DELETE();\n" +
                "\n" +
                "END $$;";
        try {
            statement = connection.createStatement();
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void deleteFinallyOperatorHistory(String email) {
        LinkedList<User> users = this.selectUsersByEmail(email);
        for (User user : users) {
            deleteFinally(user.getOperator_id());
            for (Operator operator : selectOperatorByParentId(user.getOperator_id())) {
                deleteFinally(operator.getId());
            }
            for (Operator operator : selectOperatorById(user.getOperator_id())) {
                if (operator.getParent_id() != null) {
                    if (operator.getParent_id() != "null") {
                        for (Operator operator1 : selectOperatorById(operator.getParent_id())) {
                            deleteFinally(operator1.getId());
                        }
                    }
                }
            }
        }
    }
}
