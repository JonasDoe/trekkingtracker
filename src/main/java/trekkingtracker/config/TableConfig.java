package trekkingtracker.config;

import config.Config;
import config.Setting;

/**
 * Configuration for the input table.
 */
public class TableConfig extends Config {
    /** The separator between the fields in a row */
    @Setting(descriptor = "separator", defaultValue = ",")
    public String separator;
    /** The number of header rows to be ignored */
    @Setting(descriptor = "skip_header_rows", defaultValue = "0")
    public int skipHeaderRows;
    /** The number of the column with the participant's name (first column is 1, NOT 0) */
    @Setting(descriptor = "name_column", defaultValue = "1")
    public int nameCol;
    /** The number of the column with the participant's birthday (first column is 1, NOT 0) */
    @Setting(descriptor = "birthday_column", defaultValue = "2")
    public int birthdayCol;
    /** The number of the column with the participant's starter category (first column is 1, NOT 0) */
    @Setting(descriptor = "category_column", defaultValue = "3")
    public int categoryCol;
}
