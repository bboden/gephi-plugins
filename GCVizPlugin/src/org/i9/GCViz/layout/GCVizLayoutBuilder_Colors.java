/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.i9.GCViz.layout;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;

/**
 *
 * @author Roman
 */
import org.openide.util.lookup.ServiceProvider;
 
@ServiceProvider(service = LayoutBuilder.class)
public class GCVizLayoutBuilder_Colors implements LayoutBuilder{
    
    private TestLayoutUI ui = new TestLayoutUI();
    
    @Override
    public String getName() {
        return "GC-Viz Colors";
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    @Override
    public Layout buildLayout() {
        return new ChangeColors(this);
    }
            
    private static class TestLayoutUI implements LayoutUI {

        @Override
        public String getDescription() {
            return "Layout for comparing clustering results";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return -1;
        }

        @Override
        public int getSpeedRank() {
            return -1;
        }
    }
}
