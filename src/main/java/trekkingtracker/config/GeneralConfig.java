package trekkingtracker.config;

import config.Config;
import config.NestedConfig;
import config.Setting;

import java.time.ZoneId;

/**
 * General configurations
 */
public class GeneralConfig extends Config {
    /**
     * The time zone the program is working in
     */
    @Setting(descriptor = "timezone", isOptional = true)
    public ZoneId timeZone = ZoneId.systemDefault();
    /**
     * Configuration for the input table
     */
    @NestedConfig(prefix = "table.")
    public TableConfig tableConfig;
}
