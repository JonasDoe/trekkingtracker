# TrekkingTracker
This was designed for dog trekkings, and helps the organizer to keep track of their participants and their trekking times.

# Requirements
1. A up-to-date version of the [Java Runtime Environment](https://www.java.com/de/download/manual.jsp) or the [Java Development Kit](https://www.oracle.com/java/technologies/downloads/#jdk21-windows)
2. Ideally, a `trekkingtracker.cfg` in the same folder as the TrekkingTracker `.jar` file. See the `Config` section below for more details.

# Running the program.
Download the latest `TrekkingTracker[someVersion].jar` file.
## On Windows
1. Try to run the jar on double-click. If it doesn't work, make sure that you've Java installed.
2. If 1. didn't work, press WIN+R, type `cmd` and run enter. Alternatively, open your `Start` menu, type `command` and select the command line tool. 

```shell
java -jar path/to/jarFile
```
, e.g. `java -jar C:\Users\Jonas\trekkingtracker\target\TrekkingTracker_1.0-SNAPSHOT.jar`.

# The Config
The config file `trekkingtracker.cfg` must reside in the same folder as the `.jar` file and should contain those line:
```
# the timezone the program is working in
timezone = CET
# the separator between the fields in a row
table.separator = ,
# the number of header rows to be ignored
table.skip_header_rows = 1
# the number of the column with the participant's name (must be >= 1)
table.name_column = 1
# the number of the column with the participant's birthday (must be >= 1)
table.birthday_column = 4
# the number of the column with the participant's starter category (must be >= 1)
table.category_column = 8
```
(Lines starting with `#` are just comments.) Most settings in here describe how to interpret a input file with the participants' data:
- How many lines at the top shall be skipped b/c they don't contain data but e.g. empty lines or table headers?
- In which column, start counting with `1`, can we find the participant's name?
- In which column can we find the participant's birthdate?
- In which column can we find the participant's starter category? Note: At the moment, categories must be one of `TOUR`, `DOGHIKE`, `DOGTREKKING`, `BARRIER_FREE` or `UNKNOWN`. If need arises, this can be made more flexible in future.
- How are the columns separated from each other?

Note that you don't _need_ to set up participants in a `.csv` file. Instead, you can start the program and add all participants one by one.