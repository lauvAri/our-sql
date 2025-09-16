package executor.executionEngine;

/*
执行引擎接口
 */

import common.plan.*;
import executor.common.*;
import executor.executionEngine.engineMethods.BasicExecutionEngine;
import executor.executionEngine.engineMethods.IndexExecutionEngine;
import executor.storageEngine.StorageEngine;

/**
 * 执行引擎
 */
public class ExecutionEngine {
    private final StorageEngine storage;

    public ExecutionEngine(StorageEngine storage) {
        this.storage = storage;
    }

    //执行
    public ExecutionResult execute(LogicalPlan plan) {
        try {
            storage.beginTransaction();
            Object result = dispatchExecution(plan);
            storage.commitTransaction();
            return ExecutionResult.success(result);
        } catch (ExecutionException e) {
            storage.rollbackTransaction();
            return ExecutionResult.failure(e.getMessage());
        }
    }

    //分类
    private Object dispatchExecution(LogicalPlan plan) {
        switch (plan.getOperatorType()) {
            case CREATE_TABLE:
                return BasicExecutionEngine.executeCreateTable(storage,(CreateTablePlan) plan);
            case INSERT:
                return BasicExecutionEngine.executeInsert(storage,(InsertPlan) plan);
            case SELECT:
                return BasicExecutionEngine.executeSelect(storage,(SelectPlan) plan);
            case DELETE:
                return BasicExecutionEngine.executeDelete(storage,(DeletePlan) plan);
            case CREATE_INDEX:
                return IndexExecutionEngine.executeCreateIndex(storage,(CreateIndexPlan) plan);
            case DROP_INDEX:
                return IndexExecutionEngine.executeDropIndex(storage,(DropIndexPlan) plan);
            default:
                throw new ExecutionException("Unsupported operator: " + plan.getOperatorType());
        }
    }
}