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
public class GCVizLayoutBuilder implements LayoutBuilder{
    
    private TestLayoutUI ui = new TestLayoutUI();
    
    @Override
    public String getName() {
        return "GC-Viz";
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    @Override
    public Layout buildLayout() {
        return new GCVizLayout(this);
    }
            
    private static class TestLayoutUI implements LayoutUI {

        @Override
        public String getDescription() {
            return "Layout for visualizing clustering results";
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
