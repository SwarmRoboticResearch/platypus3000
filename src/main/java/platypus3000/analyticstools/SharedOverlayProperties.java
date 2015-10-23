package platypus3000.analyticstools;

import java.util.HashMap;

/**
 *  The shared overlay properties are those properties which are same for all instances of an overlay.
 *  For example the name and the colors.
 *  They are also the rows in the settings table. So if the table is changed, the changes are automatically
 *  propagate to all instances of that overlay.
 *  It is only used internal and you should not be bothered with this class.
 */
public class SharedOverlayProperties {
    final String name; //The name of the overlay
    public boolean show_all = false; //Draw it for all robots
    public boolean show_selected = false; //Draw it for the selected robot (if show_all = false)
    public HashMap<String, DynamicColor> colorMap = new HashMap<String, DynamicColor>(); //The colors of this overlay. Synchronised with the JTable.
        // ColorChanges are made in the DynamicColor-Objects. Do not replace them!

    //Private, use getSharedProperties for ensuring all instances get the same object.
    SharedOverlayProperties(String name){
        this.name= name;
    }
}
