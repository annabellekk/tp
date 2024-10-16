package seedu.academyassist;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.academyassist.commons.core.Config;
import seedu.academyassist.commons.core.LogsCenter;
import seedu.academyassist.commons.core.Version;
import seedu.academyassist.commons.exceptions.DataLoadingException;
import seedu.academyassist.commons.util.ConfigUtil;
import seedu.academyassist.commons.util.StringUtil;
import seedu.academyassist.logic.Logic;
import seedu.academyassist.logic.LogicManager;
import seedu.academyassist.model.AddressBook;
import seedu.academyassist.model.Model;
import seedu.academyassist.model.ModelManager;
import seedu.academyassist.model.ReadOnlyAcademyAssist;
import seedu.academyassist.model.ReadOnlyUserPrefs;
import seedu.academyassist.model.UserPrefs;
import seedu.academyassist.model.util.SampleDataUtil;
import seedu.academyassist.storage.AcademyAssistStorage;
import seedu.academyassist.storage.JsonAcademyAssistStorage;
import seedu.academyassist.storage.JsonUserPrefsStorage;
import seedu.academyassist.storage.Storage;
import seedu.academyassist.storage.StorageManager;
import seedu.academyassist.storage.UserPrefsStorage;
import seedu.academyassist.ui.HelpWindow;
import seedu.academyassist.ui.Ui;
import seedu.academyassist.ui.UiManager;


/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(0, 2, 2, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());
        initLogging(config);

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        AcademyAssistStorage addressBookStorage = new JsonAcademyAssistStorage(userPrefs.getAcademyAssistFilePath());
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading {@code storage}'s address book.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAcademyAssistFilePath());

        Optional<ReadOnlyAcademyAssist> addressBookOptional;
        ReadOnlyAcademyAssist initialData;
        try {
            addressBookOptional = storage.readAcademyAssist();
            if (!addressBookOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getAcademyAssistFilePath()
                        + " populated with a sample AddressBook.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAcademyAssist);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAcademyAssistFilePath() + " could not be loaded."
                    + " Will be starting with an empty AddressBook.");
            initialData = new AddressBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            if (!configOptional.isPresent()) {
                logger.info("Creating new config file " + configFilePathUsed);
            }
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataLoadingException e) {
            logger.warning("Config file at " + configFilePathUsed + " could not be loaded."
                    + " Using default config properties.");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (!prefsOptional.isPresent()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
        if (model.getUserPrefs().isFirstTime()) {
            showHelpWindow();
            // Set isFirstTime to false and save the updated preferences
            UserPrefs updatedPrefs = new UserPrefs(model.getUserPrefs());
            updatedPrefs.setIsFirstTime(false);
            model.setUserPrefs(updatedPrefs);
            try {
                storage.saveUserPrefs(updatedPrefs);
            } catch (IOException e) {
                logger.warning("Failed to save updated user preferences: " + StringUtil.getDetails(e));
            }
        }
    }

    /**
     * Shows the help window.
     */
    private void showHelpWindow() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.show();
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
