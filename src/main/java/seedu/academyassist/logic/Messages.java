package seedu.academyassist.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.academyassist.logic.parser.Prefix;
import seedu.academyassist.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format! \n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_NRIC = "No student found with the provided NRIC";

    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d student listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
            "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_STUDENTS_FOUND_MATCHES = "%1$d student(s) found that matches the keywords.";
    public static final String MESSAGE_INVALID_NAME_FORMAT = "The name entered is invalid. "
            + "\nPlease make sure the name is between 1-100 characters and only contains alphabets and spaces.";
    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; NRIC: ")
                .append(person.getIc())
                .append("; Subjects: ");
        person.getSubjects().forEach(builder::append);
        builder.append(person.getSubjects())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

}