package org.gridlab.gat.advert.cpi;

import java.io.Serializable;

import org.gridlab.gat.advert.Advertisable;

@SuppressWarnings("serial")
public abstract class SerializedBase implements Serializable, Advertisable {
        
    private String classname;
    
    public SerializedBase() {
    }

    public SerializedBase(String classname) {
        this.classname = classname;
    }
    
    public String getClassname() {
        return classname;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }
    
    public boolean checkClassname(String classname) {
        return classname.equals(classname);
    }
}
