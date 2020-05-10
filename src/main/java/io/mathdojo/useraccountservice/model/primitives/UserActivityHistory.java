package io.mathdojo.useraccountservice.model.primitives;

import java.util.Map;
import java.util.Objects;

public class UserActivityHistory {

    private Map<String, PracticeQuestionAttemptRecord> practiceHistory;

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

        sb.append("    practiceHistory: ").append(toIndentedString(practiceHistory)).append("\n");
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
}
