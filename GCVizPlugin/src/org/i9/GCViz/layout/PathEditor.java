package org.i9.GCViz.layout;

/**
 *
 * @author boden
 */

import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JFileChooser;


public  class PathEditor implements PropertyEditor{
    
    javax.swing.JFileChooser fileChooser = new JFileChooser();
    String path="no path selected";
    
    public String getAsText(){
        if(fileChooser.getSelectedFile()!=null){
            path=fileChooser.getSelectedFile().toString();
        }
        return path;
    }
    
     public java.awt.Component getCustomEditor(){
            
         return fileChooser;
                
    }
    
    public void setAsText(String text) throws java.lang.IllegalArgumentException{
        path = text;
   }
    
    public void setValue(Object value){
        path=value.toString();
    }
    public Object getValue(){
        return path;
    }
    public boolean isPaintable(){
        return false;
    } 
    public  void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box){}
    
    public String getJavaInitializationString(){
        return "initialisation";
    }
    public String[] getTags(){
        String[] out = new String[1];
        out[0]=path;
        return out;
    }
    
   public void addPropertyChangeListener(PropertyChangeListener listener){}
   
   public void removePropertyChangeListener(PropertyChangeListener listener){}
    
    public boolean supportsCustomEditor(){
        return true;
    }
            
   }
