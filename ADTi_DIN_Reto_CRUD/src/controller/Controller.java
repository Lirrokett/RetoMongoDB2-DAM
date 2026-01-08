package controller;

import dao.DBImplementation;
import dao.ModelDAO;
import dao.MongoConnection;
import exception.ErrorMessages;
import exception.OurException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Profile;
import model.User;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Main controller class that serves as the central coordinator between
 * the user interface and the data access layer.
 */
public class Controller {

    private final ModelDAO dao;

    /**
     * Constructor used for dependency injection (tests).
     */
    public Controller(ModelDAO dao) {
        this.dao = dao;
    }

    /**
     * Main constructor.
     * Initializes MongoDB connection and DAO implementation.
     */
    public Controller() throws OurException {
        try {
            // 🔥 CONEXIÓN REAL A MONGO
            MongoConnection.connect(
                "mongodb://localhost:27017",
                "users_manager"
            );

            // DAO REAL
            dao = new DBImplementation();

        } catch (Exception e) {
            // ❌ NO USAMOS MOCK AQUÍ
            throw new OurException(ErrorMessages.DATABASE);
        }
    }

    /**
     * Shows login window.
     */
    public void showWindow(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginWindow.fxml"));
        Parent root = loader.load();

        LoginWindowController loginController = loader.getController();
        loginController.setController(this);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Log In");
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        stage.show();
    }

    // --------------------------------------------------
    // DELEGATION METHODS
    // --------------------------------------------------

    public User register(User user) throws OurException {
        return dao.register(user);
    }

    public Profile login(String credential, String password) throws OurException {
        return dao.login(credential, password);
    }

    public ArrayList<User> getUsers() throws OurException {
        return dao.getUsers();
    }

    public boolean updateUser(User user) throws OurException {
        return dao.updateUser(user);
    }

    public boolean deleteUser(int id) throws OurException {
        return dao.deleteUser(id);
    }
}
