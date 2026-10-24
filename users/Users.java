package users;

public class Users {

    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;

    public Users() {  
    }
    
    public Users(String email, String role) {
        this.email = email;
        this.role = new Role(Role.Name.valueOf(role));
        //this.id = findIdByEmail(email);
        //... 
    }

    private int findIdByEmail(String email2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findIdByEmail'");
    }

    Users(int id, String name, String email,String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = new Role(Role.Name.valueOf(role));
    }
     
    Users(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = new Role(Role.Name.valueOf(role));
    }

    public void call(String string) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'call'");
    
    }

    public void answer(String string, String string2) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'answer'");
    }

    public void call(String string, String string2) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'call'");
    }

    public Object getName(String string) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getName'");
        return null;
    }

    public Object getPhoneNumber() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'getPhoneNumber'");
        return null;
    }

    public Object getPhoneNumber(String string) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'getPhoneNumber'");
       return null;
    }

    public void endCall(String string) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'endCall'");
        
    }

    public String getName() {
        return name;
    }
}