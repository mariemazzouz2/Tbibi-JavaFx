package test;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        Connection connection=MyDataBase.getInstance().getConnection();
        Connection connection1=MyDataBase.getInstance().getConnection();

        System.out.println(connection);
        System.out.println(connection1);}
}