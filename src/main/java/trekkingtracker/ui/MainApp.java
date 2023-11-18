package trekkingtracker.ui;

import config.ConfigPreparer;
import config.SettingConverter;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import trekkingtracker.config.GeneralConfig;
import trekkingtracker.event.participantevents.ParticipantEvent;
import trekkingtracker.event.publishing.ThreadAwareEventPublisher;
import trekkingtracker.event.requestevents.ParticipantEventRequest;
import trekkingtracker.event.requestevents.ParticipantsInitRequest;
import trekkingtracker.persistence.ParticipantStore;
import trekkingtracker.ui.groups.*;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/**
 * The main application which displays the UI.
 */
public class MainApp extends Application {
    /**
     * The location of the config file
     */
    public static final String CONFIG_FILE = "trekkingtracker.cfg";
    /**
     * The config file to be created in case its missing. While this could be achieved with {@code Config#store},
     * we do it this way to also provide some config documentation.
     */
    private static final String DEFAULT_CONFIG = "# the timezone the program is working in\n" +
            "timezone = CET\n" +
            "# the separator between the fields in a row\n" +
            "table.separator = ,\n" +
            "# the number of header rows to be ignored\n" +
            "table.skip_header_rows = 1\n" +
            "# the number of the column with the participant's name (must be >= 1)\n" +
            "table.name_column = 1\n" +
            "# the number of the column with the participant's birthday (must be >= 1)\n" +
            "table.birthday_column = 4\n" +
            "# the number of the column with the participant's starter category (must be >= 1)\n" +
            "table.category_column = 8";
    /**
     * The encoding of the files to read and store
     */
    private static final String FILE_EXTENSION = "*.csv";
    /**
     * The time ZONE the application is running in
     */
    private ZoneId zone;
    /**
     * Formatter to parse the birthdays of the {@code Persons} from the initial file of registered participant
     */
    private DateTimeFormatter registeredBirthdayFormatter;
    /**
     * Static pseudo-logger and hint box for the user
     */
    private static InfoOutput infoOutput;
    /**
     * Holds the configuration for this application
     */
    private GeneralConfig config;
    /**
     * Information of be displayed once the {@link InfoOutput} is initialized.
     */
    private final List<String> initialLogEntries = new ArrayList<>();

    /**
     * Prints the given text in the info box
     *
     * @param info to be printed in the info box
     */
    public static void printInfo(String info) { //TODO as log event instead?
        if (infoOutput != null) infoOutput.printInfo(info);
    }

    @Override
    public void stop() throws Exception {
        this.config.store();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        setup();

        VBox root = new VBox();

        ThreadAwareEventPublisher eventPublisher = new ThreadAwareEventPublisher();

        final ParticipantStore participantStore = new ParticipantStore(config.tableConfig, eventPublisher,
                s -> registeredBirthdayFormatter.parse(s, LocalDate::from));
        eventPublisher.addEventListenerNonUi(ParticipantEventRequest.class, participantStore);

        AddParticipants addingParticipants = new AddParticipants(root, eventPublisher, FILE_EXTENSION);
        eventPublisher.addEventListenerUi(ParticipantEvent.class, addingParticipants);

        ParticipantModification participModify = new ParticipantModification(root, eventPublisher, zone);
        eventPublisher.addEventListenerUi(ParticipantEvent.class, participModify);

        ParticipantStart participStart = new ParticipantStart(root, eventPublisher, zone);
        eventPublisher.addEventListenerUi(ParticipantEvent.class, participStart);

        ParticipantStop participStop = new ParticipantStop(root, eventPublisher, zone);
        eventPublisher.addEventListenerUi(ParticipantEvent.class, participStop);

        Overview overview = new Overview(root, zone);
        eventPublisher.addEventListenerUi(ParticipantEvent.class, overview);

        infoOutput = new InfoOutput(root);
        this.initialLogEntries.forEach(MainApp::printInfo);

        root.getChildren().addAll(addingParticipants, participModify, participStop, infoOutput);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("TrekkingTracker");
        primaryStage.setScene(scene);
        initIcons(primaryStage);
        primaryStage.show();

        eventPublisher.publish(ParticipantsInitRequest.INSTANCE);
    }

    private void setup() throws IOException {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.isFile()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                writer.write(DEFAULT_CONFIG);
            }
            initialLogEntries.add(String.format("Config file %s not found - created a new one with default settings. In case you want to adjust them, please edit the file and restart the program.", CONFIG_FILE));
        }
        //config.registerConverter(ZoneId.class, new SettingConverter(Object::toString,ZoneId::of));
        this.config = new ConfigPreparer(new File(CONFIG_FILE)).registerConverter(ZoneId.class,
                new SettingConverter(Object::toString, ZoneId::of)).fillConfig(new GeneralConfig());
        zone = config.timeZone;//ZoneId.systemDefault();//ZoneId.of("CET");
        registeredBirthdayFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("dd.MM."))
                .appendValueReduced(ChronoField.YEAR, 2, 4,
                        LocalDate.ofEpochDay(0))
                .toFormatter()
                .withZone(zone);
    }


    /**
     * Registers application icons.
     *
     * @param stage the icons shall be registered at
     */
    private void initIcons(Stage stage) {
        ClassLoader classLoader = getClass().getClassLoader();
        ObservableList<Image> icons = stage.getIcons();
        for (int no = 1; no < 5; no++) {
            InputStream icon = classLoader.getResourceAsStream(String.format("icon%d.png", no));
            if (icon != null) icons.add(new Image(icon));
        }
    }
}
