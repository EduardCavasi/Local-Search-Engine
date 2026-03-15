package org.example;


import org.example.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try(Connection conn = DatabaseConnection.getConnection()){
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from file_info");
            while(rs.next()){
                System.out.println(rs.getString(1));
            }
            System.out.println("ok");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}