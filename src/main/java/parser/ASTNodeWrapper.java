package parser;

/**
 * AST节点包装器 - 提供对package-private AST节点的public访问
 */
public class ASTNodeWrapper {
    
    /**
     * 获取SelectNode的实例
     */
    public static SelectNode getSelectNode(ASTNode node) {
        if (node instanceof SelectNode) {
            return (SelectNode) node;
        }
        return null;
    }
    
    /**
     * 获取CreateTableNode的实例
     */
    public static CreateTableNode getCreateTableNode(ASTNode node) {
        if (node instanceof CreateTableNode) {
            return (CreateTableNode) node;
        }
        return null;
    }
    
    /**
     * 获取InsertNode的实例
     */
    public static InsertNode getInsertNode(ASTNode node) {
        if (node instanceof InsertNode) {
            return (InsertNode) node;
        }
        return null;
    }
    
    /**
     * 获取DeleteNode的实例
     */
    public static DeleteNode getDeleteNode(ASTNode node) {
        if (node instanceof DeleteNode) {
            return (DeleteNode) node;
        }
        return null;
    }
    
    /**
     * 检查节点类型
     */
    public static boolean isSelectNode(ASTNode node) {
        return node instanceof SelectNode;
    }
    
    public static boolean isCreateTableNode(ASTNode node) {
        return node instanceof CreateTableNode;
    }
    
    public static boolean isInsertNode(ASTNode node) {
        return node instanceof InsertNode;
    }
    
    public static boolean isDeleteNode(ASTNode node) {
        return node instanceof DeleteNode;
    }
}
