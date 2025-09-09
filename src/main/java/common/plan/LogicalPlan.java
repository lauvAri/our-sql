package common.plan;

public abstract class LogicalPlan {
    public enum OperatorType {
        CREATE_TABLE,
        INSERT,
        SELECT,
        DELETE
    }

    public abstract OperatorType getOperatorType();
}
