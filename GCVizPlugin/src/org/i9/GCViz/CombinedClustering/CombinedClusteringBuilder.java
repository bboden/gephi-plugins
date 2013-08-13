
package org.i9.GCViz.CombinedClustering;

import javax.swing.JPanel;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererUI;
import org.openide.util.NbBundle;


public class CombinedClusteringBuilder {
    
     public CombinedClustering getClusterer() {
        return new CombinedClustering();
    }

    public String getGamerName() {
        return "GAMer";
    }
    
    public String getDBGraphName() {
        return "DB-CSC";
    }

    public String getDescription() {
        return NbBundle.getMessage(CombinedClusteringBuilder.class, "CombinedClustering.description");
    }

    public Class getClustererClass() {
        return CombinedClustering.class;
    }

    public ClustererUI getUI() {
        return new CombinedClusteringUI();
    }

    private static class CombinedClusteringUI implements ClustererUI {

        GamerSettingPanel panel;

        public JPanel getPanel() {
            panel = new GamerSettingPanel();
            return panel;
        }

        public void setup(Clusterer clusterer) {
            panel.setup();
        }

        public void unsetup() {
            panel.unsetup();
            panel = null;
        }
    } 
}
