package tinyjs;

import java.util.HashMap;


public class Env extends HashMap<String, Evaluate> {

    public Env Parent;
    public boolean alreadyBeenCreated = false;

    public Env() {
        this.Parent = null;
    }

    private boolean recursiveBind(String ide, Evaluate value, boolean declaration) {
        if (this.containsKey(ide)) {
            if (declaration) {
                return false;
            } else {
                this.put(ide, value);
                return true;
            }
        } else {
            if (this.Parent != null) {
                return this.Parent.recursiveBind(ide, value, declaration);
            } else {
                return false;
            }
        }
    }

    public Evaluate apply(String ide) {
        if (this.containsKey(ide)) {
            return this.get(ide);
        } else if (this.Parent != null) {
            return this.Parent.apply(ide);
        } else {
            System.out.println("Cannot found \"" + ide + "\" in the Environment.");
            System.exit(1);
            return null;
        }
    }

    public void bind(String ide, Evaluate value, boolean declaration) {
        boolean bound = this.recursiveBind(ide, value, declaration);
        if (!bound) {
            this.put(ide, value);
        }
    }
}
