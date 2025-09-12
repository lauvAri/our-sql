# 增强的语法规则

## 新增产生式规则

### 复杂WHERE条件支持
```bnf
<WhereClause> ::= WHERE <LogicalExpression> | ε

<LogicalExpression> ::= <LogicalTerm> <LogicalExpressionTail>

<LogicalExpressionTail> ::= OR <LogicalTerm> <LogicalExpressionTail> | ε

<LogicalTerm> ::= <LogicalFactor> <LogicalTermTail>

<LogicalTermTail> ::= AND <LogicalFactor> <LogicalTermTail> | ε

<LogicalFactor> ::= NOT <LogicalFactor> | <Condition> | ( <LogicalExpression> )

<Condition> ::= <ColumnName> <Operator> <Value>
              | <ColumnName> IS NULL
              | <ColumnName> IS NOT NULL
              | <ColumnName> IN ( <ValueList> )

<ValueList> ::= <Value> <ValueListTail>

<ValueListTail> ::= , <Value> <ValueListTail> | ε
```

## 对应的新增产生式编号

31. `WhereClause → WHERE LogicalExpression`
32. `WhereClause → ε`
33. `LogicalExpression → LogicalTerm LogicalExpressionTail`
34. `LogicalExpressionTail → OR LogicalTerm LogicalExpressionTail`
35. `LogicalExpressionTail → ε`
36. `LogicalTerm → LogicalFactor LogicalTermTail`
37. `LogicalTermTail → AND LogicalFactor LogicalTermTail`
38. `LogicalTermTail → ε`
39. `LogicalFactor → NOT LogicalFactor`
40. `LogicalFactor → Condition`
41. `LogicalFactor → ( LogicalExpression )`
42. `Condition → ID Operator Value`
43. `Condition → ID IS NULL`
44. `Condition → ID IS NOT NULL`
45. `Condition → ID IN ( ValueList )`

这样可以支持复杂的WHERE条件如：
- `WHERE age > 18 AND status = 'active'`
- `WHERE age > 18 OR role IN ('admin', 'manager')`
- `WHERE NOT (age < 18 AND status = 'inactive')`
- `WHERE name IS NOT NULL`
