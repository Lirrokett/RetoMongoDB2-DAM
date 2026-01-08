package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import exception.ErrorMessages;
import exception.OurException;

import java.util.ArrayList;
import java.util.List;

import model.Admin;
import model.Gender;
import model.Profile;
import model.User;

import org.bson.Document;

import dao.ConnectionRetention;

public class DBImplementation implements ModelDAO {

    private final MongoCollection<Document> collection;
    private static final String ROOT_ID = "profiles_root";

    public DBImplementation() throws OurException {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            collection = db.getCollection("db_profile");
        } catch (Exception e) {
            throw new OurException(ErrorMessages.DATABASE);
        }
    }

    private Document getRoot() throws OurException {
        Document root = collection.find(eq("_id", ROOT_ID)).first();
        if (root == null) {
            throw new OurException("profiles_root not found in database");
        }
        return root;
    }

    @Override
    public User register(User user) throws OurException {
        ConnectionRetention.retain();
        
        try {
            int newId = collection.find()
                    .into(new ArrayList<>())
                    .stream()
                    .mapToInt(d -> d.getInteger("P_ID"))
                    .max()
                    .orElse(0) + 1;

            Document newUser = new Document()
                    .append("P_ID", newId)
                    .append("ROLE", "USER")
                    .append("P_EMAIL", user.getEmail())
                    .append("P_USERNAME", user.getUsername())
                    .append("P_PASSWORD", user.getPassword())
                    .append("P_NAME", user.getName())
                    .append("P_LASTNAME", user.getLastname())
                    .append("P_TELEPHONE", user.getTelephone())
                    .append("U_GENDER", user.getGender().toString())
                    .append("U_CARD", user.getCard());

            collection.insertOne(newUser);

            user.setId(newId);
            return user;

        } catch (Exception e) {
            throw new OurException("Error registering user: " + e.getMessage());
        }
    }

    @Override
    public Profile login(String credential, String password) throws OurException {
        try {
            Document d = collection.find(
                    eq("P_PASSWORD", password)
            ).filter(
                    eq("P_EMAIL", credential)
            ).first();

            if (d == null) {
                d = collection.find(
                        eq("P_PASSWORD", password)
                ).filter(
                        eq("P_USERNAME", credential)
                ).first();
            }

            if (d == null) {
                throw new OurException("Invalid credentials");
            }

            if ("ADMIN".equals(d.getString("ROLE"))) {
                Admin admin = new Admin(
                        d.getString("P_EMAIL"),
                        d.getString("P_USERNAME"),
                        d.getString("P_PASSWORD"),
                        d.getString("P_NAME"),
                        d.getString("P_LASTNAME"),
                        d.getString("P_TELEPHONE"),
                        d.getString("A_CURRENT_ACCOUNT")
                );
                admin.setId(d.getInteger("P_ID"));
                return admin;
            }

            User user = new User(
                    d.getString("P_EMAIL"),
                    d.getString("P_USERNAME"),
                    d.getString("P_PASSWORD"),
                    d.getString("P_NAME"),
                    d.getString("P_LASTNAME"),
                    d.getString("P_TELEPHONE"),
                    Gender.valueOf(d.getString("U_GENDER")),
                    d.getString("U_CARD")
            );
            user.setId(d.getInteger("P_ID"));
            return user;

        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            throw new OurException("Login error: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<User> getUsers() throws OurException {
        ConnectionRetention.retain();
        
        ArrayList<User> users = new ArrayList<>();

        try {
            for (Document d : collection.find(eq("ROLE", "USER"))) {
                User user = new User(
                        d.getString("P_EMAIL"),
                        d.getString("P_USERNAME"),
                        d.getString("P_PASSWORD"),
                        d.getString("P_NAME"),
                        d.getString("P_LASTNAME"),
                        d.getString("P_TELEPHONE"),
                        Gender.valueOf(d.getString("U_GENDER")),
                        d.getString("U_CARD")
                );
                user.setId(d.getInteger("P_ID"));
                users.add(user);
                /*llamar aqui el retener conexion*/
            }
            return users;

        } catch (Exception e) {
            throw new OurException("Error retrieving users: " + e.getMessage());
        }
    }

    @Override
    public boolean updateUser(User user) throws OurException {
        ConnectionRetention.retain();
        
        try {
            collection.updateOne(
                    eq("P_ID", user.getId()),
                    set("P_EMAIL", user.getEmail())
            );

            collection.updateOne(eq("P_ID", user.getId()), set("P_USERNAME", user.getUsername()));
            collection.updateOne(eq("P_ID", user.getId()), set("P_PASSWORD", user.getPassword()));
            collection.updateOne(eq("P_ID", user.getId()), set("P_NAME", user.getName()));
            collection.updateOne(eq("P_ID", user.getId()), set("P_LASTNAME", user.getLastname()));
            collection.updateOne(eq("P_ID", user.getId()), set("P_TELEPHONE", user.getTelephone()));
            collection.updateOne(eq("P_ID", user.getId()), set("U_GENDER", user.getGender().toString()));
            collection.updateOne(eq("P_ID", user.getId()), set("U_CARD", user.getCard()));

            return true;

        } catch (Exception e) {
            throw new OurException("Error updating user: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteUser(int id) throws OurException {
        ConnectionRetention.retain();
        
        try {
            collection.deleteOne(eq("P_ID", id));
            return true;

        } catch (Exception e) {
            throw new OurException("Error deleting user: " + e.getMessage());
        }
    }
    
    /*aqui hacer un metodo para retener la conexion*/

}
