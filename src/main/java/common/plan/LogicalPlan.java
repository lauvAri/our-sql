package common.plan;

public abstract class LogicalPlan {
    public enum OperatorType {
        CREATE_TABLE,
        INSERT,
        SELECT,
        DELETE,
        CREATE_INDEX,
        DROP_INDEX,
    }

    public abstract OperatorType getOperatorType();
}
