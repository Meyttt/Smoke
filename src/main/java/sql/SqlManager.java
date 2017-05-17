package sql;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

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
        LinkedList<HashMap<String,String>> selected = new LinkedList<HashMap<String, String>>();

        return resultSet;
    }

    public ResultSet SELECT(String table, String column, String condition) {
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String request = "SELECT " + column + " FROM " + table + " WHERE " + condition;
            if (debug) System.out.println("request = " +request);
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
        if(output.length()>1)
        output.deleteCharAt(output.length() - 1);
        output.append(")");
        return output.toString();
    }
//from there

    public LinkedList<User> selectUsersByEmail(String email){
        Statement statement=null;
        ResultSet resultSet= null;
        LinkedList<User> users = new LinkedList<User>();
        String request= "SELECT * FROM t_user WHERE email='"+email+"';";
        try{
            statement=connection.createStatement();
            resultSet=statement.executeQuery(request);
            while (resultSet.next()){
                users.add(new User(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public LinkedList<Operator> selectOperatorById(String id){
        Statement statement=null;
        ResultSet resultSet=null;
        String request="SELECT * FROM t_operator WHERE id='"+id+"';";
        LinkedList<Operator> operators = new LinkedList<Operator>();
        try{
            statement=connection.createStatement();
            resultSet=statement.executeQuery(request);
            while (resultSet.next()){
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

    public void changeUser(User user){
        Statement statement=null;
        java.util.Date date = new java.util.Date();
        String request = "UPDATE t_user SET email='"+user.getEmail()+" "+date+
                "', login='"+ user.getName()+" "+date+"' WHERE id=" +user.getId()+";";
        try{
            statement=connection.createStatement();
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void changeUser(String email){
        LinkedList<User> users = this.selectUsersByEmail(email);
        for(User user:users) {
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
}
