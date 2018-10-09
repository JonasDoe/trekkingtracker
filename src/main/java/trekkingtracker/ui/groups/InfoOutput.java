package trekkingtracker.ui.groups;

import javafx.scene.Group;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import trekkingtracker.ui.utils.UiUtils;

/** Simple text area which outputs information */
public class InfoOutput extends Group {
    /** Information output */
    private final TextArea infoArea;
    
    /**
     * Creates a new {@code InfoOutput}.
     *
     * @param parent
     *         the @code InfoOutput} will be put on
     */
    public InfoOutput(Pane parent) {
        GridPane mainArea = UiUtils.createFramedArea(parent, "Info");
        infoArea = new TextArea();
        infoArea.setEditable(false);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);
        columnConstraints.setHgrow(Priority.ALWAYS);
        mainArea.getColumnConstraints().add(columnConstraints);
        mainArea.addColumn(0, infoArea);
    }
    
    /**
     * Prints the given text in the info box
     *
     * @param info
     *         to be printed in the info box
     */
    public void printInfo(String info) {
        if (info != null) UiUtils.uiJob(() -> infoArea.appendText(info + "\n"));
    }
}
