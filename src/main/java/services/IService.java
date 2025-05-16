// src/main/java/services/IService.java
package services;

import exceptions.ValidationException;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void ajouter(T t) throws SQLException, ValidationException;
    void modifier(T t) throws SQLException, ValidationException;
    void supprimer(int id) throws SQLException;
    List<T> afficher() throws SQLException;
}