package trekkingtracker.persistence;

import trekkingtracker.Utils;
import trekkingtracker.config.TableConfig;
import trekkingtracker.data.Person;
import trekkingtracker.data.PersonImpl;
import trekkingtracker.event.participantevents.ParticipantInputChangedEvent;
import trekkingtracker.event.publishing.EventPublisher;
import trekkingtracker.event.requestevents.*;
import trekkingtracker.ui.MainApp;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores and restores the trekking related participants data. Can run in two modes, once which only appends updates
 * and one which replaces all data with the latest version
 */
public class ParticipantStore implements ParticipantOperator {
    /** Store of the person data */
    public static final File TREKKING_DATA = new File("participant_data.dat");
    /** Header row, to be put at the beginning of a file */
    private static final String HEADER = "# Name;Birthday;Category;Number;Start;Stop;Finisher";
    /** Ensures there is only one file access at a time */
    private final Lock lock = new ReentrantLock();
    /**
     * Information about the participants. A value of {@code null} indicates that {@link #restore} has not been
     * called successfully yet.
     */
    private List<Person> participantData = null;
    // Tells about the structure of the input table
    private TableConfig config;
    private final EventPublisher eventPublisher;
    /** Parses the birthdays of the {@code Persons} from the initial file of signed participants */
    private final Function<String, LocalDate> registeredParticipantBirthDayFormat;
    /** Ensures now start number is linked to more than one participant */
    private Map<Integer, Person> startNumbersToParticipants;
    
    /**
     * Creates a new {@code ParticipantStore}.
     *
     * @param config
     *         tells about the structure of the input table
     * @param eventPublisher
     *         used to propagate events
     * @param registeredParticipantBirthDayFormat
     *         Parses the birthdays of the {@code Persons} from the initial file of registered participants
     */
    public ParticipantStore(TableConfig config, EventPublisher eventPublisher,
                            Function<String, LocalDate> registeredParticipantBirthDayFormat) {
        this.config = config;
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.registeredParticipantBirthDayFormat = Objects.requireNonNull(registeredParticipantBirthDayFormat);
    }
    
    /**
     * Attempts to initialize the store by loading the latest state from {@link #TREKKING_DATA}.
     *
     * @return {@code true} if the initialization was successful, otherwise {@code false}
     */
    private boolean restore() {
        try {
            List<Person> restoredState = readExistingData().orElse(null);
            if (restoredState == null || restoredState.size() == 0) return false;
            else {
                processStartNumbers(Utils.getLatestStateView(restoredState));
                participantData = restoredState;
                return true;
            }
        } catch (Exception e) {
            MainApp.printInfo("Could not restore the previous state.");
            return false;
        }
    }
    
    @Override
    public void handle(final ParticipantEventRequest request) {
        lock.lock();
        try {
            if (request instanceof ParticipantUpdateRequest) {
                ParticipantUpdateRequest updateRequest = (ParticipantUpdateRequest) request;
                boolean success = updatePerson(updateRequest.getOldValue(), updateRequest.getNewValue());
                if (success) eventPublisher.publish(updateRequest.asEvent());
            } else if (request instanceof ParticipantInputChangeRequest) {
                ParticipantInputChangeRequest changeRequest = (ParticipantInputChangeRequest) request;
                boolean success = setInput(changeRequest.getInput());
                if (success) eventPublisher.publish(changeRequest.asEvent());
            } else if (request instanceof ParticipantInputResetRequest) {
                ParticipantInputResetRequest resetRequest = (ParticipantInputResetRequest) request;
                boolean success = reset(resetRequest.getStartInput(), resetRequest.getCharset());
                if (success) eventPublisher.publish(new ParticipantInputChangedEvent(participantData));
            } else if (request instanceof ParticipantsInitRequest) {
                /*boolean success = */
                restore();
                /*if (success)*/
                eventPublisher.publish(new ParticipantInputChangedEvent(participantData));
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Processes a participant's update.
     *
     * @param oldP
     *         the previous state of the participant
     * @param newP
     *         the new state of the participant
     * @return {@code true} if the update was successful, otherwise {@code false}
     */
    private boolean updatePerson(Person oldP, Person newP) {
        try {
            checkInit();
            //adding new person is only allowed if person does not exist already
            if (oldP == null && participantData.contains(newP)) {
                String error = newP + " does already exist.";
                MainApp.printInfo(error);
                return false;
            }
            
            Integer number = newP.getNumber();
            if (number != null) {
                Person existingWithNumber = startNumbersToParticipants.get(number);
                if (existingWithNumber != null && !existingWithNumber.equals(newP)) {
                    MainApp.printInfo(
                            String.format("Starting number %d is in use by %s and %s. Please fix that immediately!",
                                    number, newP, existingWithNumber));
                    return false;
                }
            }
            participantData.add(newP);
            storeSinglePerson(newP);
            MainApp.printInfo(String.format("Stored updates for %s.", newP));
            return true;
        } catch (Exception e) {
            MainApp.printInfo(String.format("Could not store %s.", newP));
            return false;
        }
        
    }
    
    /**
     * Sets the underlying data.
     *
     * @param participants
     *         the data to set
     * @return {@code true} if the update was successful, otherwise {@code false}
     */
    private boolean setInput(final Collection<Person> participants) {
        try {
            if (TREKKING_DATA.exists()) backupData();
            TREKKING_DATA.createNewFile();
            if (!TREKKING_DATA.exists()) {
                //MainApp.printInfo(String.format("Could not create file %s.", TREKKING_DATA));
                return false;
            }
    
            processStartNumbers(Utils.getLatestStateView(participants));
            participantData = new ArrayList<>(participants);
            startNumbersToParticipants = new HashMap<>();
            for (Person participant : participantData)
                startNumbersToParticipants.put(participant.getNumber(), participant);
    
            storeAllData();
            // eventPublisher.publish(new ParticipantInputChangedEvent(participantData));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Resets the latest state (but creates a backup of it).
     *
     * @param initialParticipantData
     *         the {@code File} holding the initial data with registered participants
     * @param charset
     *         the {@code Charset} of the given file
     * @return {@code true} if the reset was successful, otherwise {@code false}
     */
    private boolean reset(File initialParticipantData, final Charset charset) {
        try {
            List<Person> newState = readInitialData(initialParticipantData, charset).orElse(null);
            setInput(newState);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Renames an existing data file so it won't be overridden. The new file name will end with {@code .bak} plus a
     * number.
     *
     * @throws IOException
     *         if renaming the data file fails
     */
    private void backupData() throws IOException {
        File freeBackupFile = new File(TREKKING_DATA.toString());
        if (Files.readAllBytes(freeBackupFile.toPath()).length == 0) return;
        int fileNumber = 0;
        while (freeBackupFile.exists()) {
            freeBackupFile = new File(TREKKING_DATA.toString() + ".bak" + ++fileNumber);
        }
        boolean renamed = TREKKING_DATA.renameTo(freeBackupFile);
        MainApp.printInfo(String.format("Creating backup file '%s' for old data.", freeBackupFile));
        if (!renamed) throw new IOException(String.format("Could not create backup file %s.", freeBackupFile));
    }
    
    /**
     * Stores the state of a single participant in the data file
     *
     * @param toStore
     *         to be persisted in the data file
     * @throws IOException
     *         if writing into the data file fails
     */
    private void storeSinglePerson(Person toStore) throws IOException {
        checkInit();
        writeData("\n" + createDataString(toStore));
    }
    
    /**
     * Stores the state of all participants in the data file.
     *
     * @throws IOException
     *         if writing into the data file fails
     */
    private void storeAllData() throws IOException {
        checkInit();
        
        StringJoiner lines = new StringJoiner("\n");
        lines.add(HEADER);
        for (Person toWrite : participantData)
            lines.add(createDataString(toWrite));
        writeData(lines.toString());
    }
    
    /**
     * Serializes a given {@code Person} to a {@code String} to be written into the data file.
     *
     * @param toWrite
     *         to be serialized to a {@code String}
     * @return the serialization {@code String}
     */
    private String createDataString(final Person toWrite) {
        StringJoiner line = new StringJoiner(";");
        String sanitizedName = FileUtils.sanitizeName(toWrite.getName());
        if (!sanitizedName.equals(toWrite.getName())) MainApp.printInfo(
                toWrite.getName() + " was stripped by invalid characters.");
        line.add(sanitizedName);
        line.add(toWrite.getBirthday().toString());
        Person.Category category = toWrite.getCategory();
        line.add(category != null ? category.toString() : "");
        Integer number = toWrite.getNumber();
        line.add(number != null ? number.toString() : "");
        Instant start = toWrite.getStart();
        line.add(start != null ? DateTimeFormatter.ISO_INSTANT.format(start) : "");
        Instant stop = toWrite.getStop();
        line.add(stop != null ? DateTimeFormatter.ISO_INSTANT.format(stop) : "");
        line.add(Boolean.toString(toWrite.isFinisher()));
        return line.toString();
    }
    
    /**
     * Writes a given data {@code String} containing information about one or more participants into the data file.
     *
     * @param dataToWrite
     *         containts the information about one or more participants to be persisted in the data file
     * @throws IOException
     *         if writing into the data file fails
     */
    private void writeData(String dataToWrite) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(TREKKING_DATA, true),
                StandardCharsets.UTF_8)) {
            writer.write(dataToWrite);
        }
    }
    
    /**
     * Checks whether this store is already initialized and throws an {@code IllegalStateException} otherwise.
     *
     * @throws IllegalStateException
     *         if this store is not initialized yet
     */
    private void checkInit() {
        if (participantData == null) throw new IllegalStateException("Updater has not been initialized yet.");
    }
    
    /**
     * Reads the existing data file to restore a persisted state of participants.
     *
     * @return the latest state of all participants
     */
    private Optional<List<Person>> readExistingData() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(TREKKING_DATA), StandardCharsets.UTF_8))) {
            Iterable<String> lines = () -> FileUtils.lineIterator(reader);
            List<Person> parsedPersons = new ArrayList<>();
            int infoCount = HEADER.split(";").length;
            for (String line : lines) {
                String[] items = line.split(";", infoCount);
                String name = items[0];
                FileUtils.sanitizeName(name);
                String birthdayIdent = items[1];
                String catIdent = items[2];
                String numberIdent = items[3];
                String startIdent = items[4];
                String stopIdent = items[5];
                String finisherIdent = items[6];
                PersonImpl parsed = new PersonImpl(name, LocalDate.parse(birthdayIdent));
                if (isValid(catIdent)) parsed.setCategory(FileUtils.parseCategory(catIdent));
                if (isValid(numberIdent)) parsed.setNumber(Integer.parseInt(numberIdent));
                if (isValid(startIdent)) parsed.setStart(Instant.parse(startIdent));
                if (isValid(stopIdent)) parsed.setStop(Instant.parse(stopIdent));
                if (isValid(finisherIdent)) parsed.setFinished(Boolean.parseBoolean(finisherIdent));
                parsedPersons.add(parsed);
            }
            return Optional.of(parsedPersons);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Chechs whether a given {@code String} has the right form to be parsed.
     *
     * @param toCheck
     *         the {@code String} to be checked
     * @return {@code true} if the {@code String} is valid, otherwise {@code false}
     */
    private boolean isValid(String toCheck) {
        return toCheck != null && !toCheck.isEmpty();
    }
    
    /**
     * Reads the initial participant data from the file with the registered participants.
     *
     * @param initialParticipantData
     *         the file holding the registered participants
     * @param cs
     *         the {@code Charset} of the given file
     * @return all registered participants
     */
    private Optional<List<Person>> readInitialData(File initialParticipantData, Charset cs) {
        List<Person> parsedPersons = new ArrayList<>();
        try (BufferedReader lineReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(initialParticipantData), cs))) {
            Iterator<String> lines = FileUtils.lineIterator(lineReader);
            for (int i = 0; i < config.skipHeaderRows; i++) if (lines.hasNext()) lines.next();
            while (lines.hasNext()) {
                String line = lines.next();
                String[] items = line.split(config.separator);
                String name = items[config.nameCol - 1].trim();
                String birthdayIdent = items[config.birthdayCol - 1].trim();
                String categoryIdent = items[config.categoryCol - 1];
                Person.Category cat = FileUtils.parseCategory(categoryIdent);
                PersonImpl parsed = new PersonImpl(name, registeredParticipantBirthDayFormat.apply(birthdayIdent));
                parsed.setCategory(cat);
                parsedPersons.add(parsed);
            }
            List<Person> duplicates = parsedPersons.stream()
                                                   .collect(Collectors.groupingBy(Function.identity()))
                                                   .entrySet()
                                                   .stream()
                                                   .filter(e -> e.getValue().size() > 1)
                                                   .map(Map.Entry::getKey)
                                                   .collect(Collectors.toList());
            if (!duplicates.isEmpty()) {
                String pluralIndicator = duplicates.size() > 1 ? "s" : "";
                MainApp.printInfo(String.format("Duplicate person%s: %s", pluralIndicator, duplicates));
            }
            return Optional.of(parsedPersons);
        } catch (Exception e) {
            MainApp.printInfo(String.format(
                    "Could not read initial data file: %s (by %s). Did you set up the config file %s properly?",
                    e.getMessage(), e.getCause(), MainApp.CONFIG_FILE));
            return Optional.empty();
        }
    }
    
    /**
     * Checks that no start number is already in use by another participant. If {@link #startNumbersToParticipants}
     * is not initialized yet, it will be initialized in here.
     *
     * @param participants
     *         to be checked that they are not linked to any start numbers linked to other {@code Persons}
     */
    private void processStartNumbers(Set<Person> participants) {
        boolean initStartNumbersMap;
        if (startNumbersToParticipants == null) {
            initStartNumbersMap = true;
            startNumbersToParticipants = new HashMap<>();
        } else initStartNumbersMap = false;
        for (Person participant : participants) {
            Integer participantNumber = participant.getNumber();
            if (participantNumber != null) {
                Person existing = initStartNumbersMap ? startNumbersToParticipants.computeIfAbsent(participantNumber,
                        x -> participant) : startNumbersToParticipants.get(participantNumber);
                if (existing != null && !existing.equals(participant)) {
                    String errorMessage = String.format(
                            "Start number %d is in use by %s and %s. Please fix that immediately!", participantNumber,
                            existing, participant);
                    MainApp.printInfo(errorMessage);
                }
            }
        }
    }
}
