package org.example;

import org.json.JSONObject;

import java.sql.*;
import java.util.Objects;

public class DatabaseProcessor {

    Connection con;

    public DatabaseProcessor(String address, String password) throws SQLException {
        this.con = DriverManager.getConnection(address, "root", password);
        System.out.println("Connected");
    }

    public String run_query(Json_POJO data, int command) throws SQLException {
        if(command == 0){
            PreparedStatement stmt_1 = this.con.prepareStatement("SELECT * FROM goods WHERE good_id = ?");
            stmt_1.setInt(1, data.getGood_id());
            JSONObject data_json = new JSONObject();
            ResultSet resultSet = stmt_1.executeQuery();
            resultSet.next();
            data_json.put("good_id", resultSet.getInt("good_id"));
            data_json.put("name", resultSet.getString("name"));
            data_json.put("amount", resultSet.getInt("amount"));
            data_json.put("price", resultSet.getInt("price"));
            data_json.put("category_id", resultSet.getInt("category_id"));
            return data_json.toString();
        }
        else if (command == 1){
            try{
                PreparedStatement stmt_1 = this.con.prepareStatement("INSERT INTO goods (name, amount, price, category_id) VALUES (?, ?, ?, ?)");
                stmt_1.setString(1, data.getName());
                stmt_1.setInt(2, data.getAmount());
                stmt_1.setInt(3, data.getPrice());
                stmt_1.setInt(4, data.getCategory_id());
                stmt_1.executeUpdate();
            }
            catch(SQLIntegrityConstraintViolationException ignored){}

            PreparedStatement stmt_2 = this.con.prepareStatement("SELECT good_id FROM goods WHERE name = ? AND amount = ? AND price = ?");
            stmt_2.setString(1, data.getName());
            stmt_2.setInt(2, data.getAmount());
            stmt_2.setInt(3, data.getPrice());
            ResultSet resultSet = stmt_2.executeQuery();
            resultSet.next();
            return String.valueOf(resultSet.getInt(1));
        }
        else if(command == 2){

            PreparedStatement stmt_1 = this.con.prepareStatement("SELECT * FROM goods WHERE good_id = ?");
            stmt_1.setInt(1, data.getGood_id());
            ResultSet resultSet = stmt_1.executeQuery();
            resultSet.next();
            if (Objects.equals(data.getName(), null)){
                data.setName(resultSet.getString("name"));
            }
            if (data.getCategory_id() == 0){
                data.setCategory_id(resultSet.getInt("category_id"));
            }
            PreparedStatement stmt_2 = this.con.prepareStatement("UPDATE goods SET name = ?, amount = amount + ?, price = price + ?, category_id = ? WHERE good_id = ?");
            stmt_2.setString(1, data.getName());
            stmt_2.setInt(2, data.getAmount());
            stmt_2.setInt(3, data.getPrice());
            stmt_2.setInt(4, data.getCategory_id());
            stmt_2.setInt(5, data.getGood_id());
            System.out.println(stmt_2);
            JSONObject data_json = new JSONObject();
            stmt_2.executeUpdate();
            return null;
        }
        else if(command == 3){
            PreparedStatement stmt_1 = this.con.prepareStatement("DELETE FROM goods WHERE good_id = ?");
            stmt_1.setInt(1, data.getGood_id());
            System.out.println(stmt_1);
            JSONObject data_json = new JSONObject();
            stmt_1.executeUpdate();
            return null;
        }
        return null;
    }

}
