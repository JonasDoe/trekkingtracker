package trekkingtracker.ui.utils;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

/** {@code GridPane} which allows to access the containing collabsible {@code TitledPane} */
public class CollapsibleGridPane extends GridPane {
    /** The containing collabsible {@code TitledPane} */
    private final TitledPane collapsibleContainer;
    
    /**
     * Creteas a new {@code CollapsibleGridPane}.
     *
     * @param collapsibleContainer
     *         the containing collabsible {@code TitledPane}
     */
    public CollapsibleGridPane(TitledPane collapsibleContainer) {
        this.collapsibleContainer = collapsibleContainer;
    }
    
    /**
     * Retunrs the containing collabsible {@code TitledPane}.
     *
     * @return the containing collabsible {@code TitledPane}
     */
    public TitledPane getCollapsibleContainer() {
        return collapsibleContainer;
    }
}
