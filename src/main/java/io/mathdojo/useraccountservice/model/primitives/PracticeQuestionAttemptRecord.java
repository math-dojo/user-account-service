package io.mathdojo.useraccountservice.model.primitives;

import java.util.Objects;

public class PracticeQuestionAttemptRecord {

    private Integer numberOfAttempts;
    private Boolean solved;

    public PracticeQuestionAttemptRecord(Integer numberOfAttempts, Boolean solved) {
        this.numberOfAttempts = numberOfAttempts;
        this.solved = solved;
    }

    public Integer getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(Integer numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PracticeQuestionAttemptRecord practiceQuestionAttemptRecord = (PracticeQuestionAttemptRecord) o;
        return Objects.equals(this.solved, practiceQuestionAttemptRecord.solved)
                && Objects.equals(this.numberOfAttempts, practiceQuestionAttemptRecord.numberOfAttempts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(solved, numberOfAttempts);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PracticeQuestionAttemptRecord {\n");

        sb.append("    solved: ").append(toIndentedString(solved)).append("\n");
        sb.append("    numberOfAttempts: ").append(toIndentedString(numberOfAttempts)).append("\n");
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
