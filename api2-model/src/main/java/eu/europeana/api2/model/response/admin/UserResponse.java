package eu.europeana.api2.model.response.admin;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.model.request.admin.UserCreate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
public class UserResponse extends ApiResponse {

    public UserResponse() {
        super();
        // used by jackson
    }

    public UserResponse(String apikey) {
        super(apikey);
    }

    private List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }

    public class User extends UserCreate {

        public long getId() {
            return id;
        }

        @Override
        public String getPassword() {
            return null;
        }

    }
}
