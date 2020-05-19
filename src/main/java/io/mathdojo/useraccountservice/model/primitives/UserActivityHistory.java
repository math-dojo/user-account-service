package io.mathdojo.useraccountservice.model.primitives;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserActivityHistory {

    private Map<String, PracticeQuestionAttemptRecord> practiceHistory;

    public UserActivityHistory() {
        practiceHistory = new HashMap<String, PracticeQuestionAttemptRecord>();
    }

    public Map<String, PracticeQuestionAttemptRecord> getPracticeHistory() {
        return practiceHistory;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserActivityHistory userActivityHistory = (UserActivityHistory) o;
        return Objects.equals(this.practiceHistory, userActivityHistory.practiceHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(practiceHistory);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserActivityHistory {\n");

        sb.append("    practiceHistory: ").append(toIndentedString(printMapProperties(practiceHistory))).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    private String printMapProperties(Map<String, ?> map) {
        String mapAsString = map.keySet().stream().map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }
}
