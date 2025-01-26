package core.eval;

import core.ast.expression.ArrayLiteral;
import core.ast.expression.AssignExpression;
import core.ast.expression.BooleanLiteral;
import core.ast.expression.CallExpression;
import core.ast.expression.FunctionLiteral;
import core.ast.expression.HashLiteral;
import core.ast.expression.Identifier;
import core.ast.expression.IfExpression;
import core.ast.expression.IndexExpression;
import core.ast.expression.InfixExpression;
import core.ast.expression.IntegerLiteral;
import core.ast.expression.PrefixExpression;
import core.ast.expression.StringLiteral;
import core.ast.expression.WhileExpression;
import core.ast.statement.BlockStatement;
import core.ast.statement.ExpressionStatement;
import core.ast.statement.ReturnStatement;
import core.ast.statement.Statement;
import core.ast.statement.VarStatement;
import core.ast.expression.Expression;
import core.ast.expression.ForExpression;
import core.ast.Program;
import core.ast.base.ASTNode;
import core.ast.base.ASTNodeType;
import core.env.Environment;
import core.env.Error;
import core.env.Function;
import core.env.Hash;
import core.env.HashKey;
import core.env.HashPair;
import core.env.NULL;
import core.env.Int;
import core.env.Array;
import core.env.Bool;
import core.env.Break;
import core.env.Continue;
import core.env.Obj;
import core.env.ObjType;
import core.env.ReturnValue;
import core.env.Str;
import core.env.builtin.BuiltinFunc;
import core.env.builtin.Builtins;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator {
    private Builtins builtin = new Builtins();

    public Obj eval(ASTNode node, Environment env) {
        switch (node.type()) {
            case Program:
                return evalProgram((Program) node, env);
            case ExpressionStatement:
                ExpressionStatement es = (ExpressionStatement) node;
                return eval(es.getExpression(), env);
            case BlockStatement:
                return evalBlockStatement((BlockStatement) node, env);
            case ReturnStatement:
                return evalReturnStatement((ReturnStatement) node, env);
            case BreakStatement:
                return new Break();
            case ContinueStatement:
                return new Continue();
            case IntegerLiteral:
                return new Int(((IntegerLiteral) node).getValue());
            case BooleanLiteral:
                return new Bool(((BooleanLiteral) node).getValue());
            case StringLiteral:
                return new Str(((StringLiteral) node).getValue());
            case Identifier:
                return evalIdentifier((Identifier) node, env);
            case VarStatement:
            {
                VarStatement vs = (VarStatement) node;
                Obj val = eval(vs.getValue(), env);
                if (val.type() == ObjType.ERROR) {
                    return val;
                }
                env.define(vs.getIdentifier().getValue(), val);
                return val;
            }
            case AssignExpression:
                return evalAssignExpression((AssignExpression) node, env);

            case PrefixExpression: {
                PrefixExpression pe = (PrefixExpression) node;
                Obj right = eval(pe.getRight(),env);
                String op = pe.getOp();
                return evalPrefixExpression(op, right);
            }
            case InfixExpression: {
                InfixExpression ie = (InfixExpression) node;
                Obj left = eval(ie.getLeft(), env);
                if (left.type() == ObjType.ERROR) {
                    return left;
                }
                Obj right = eval(ie.getRight(), env);
                if (right.type() == ObjType.ERROR) {
                    return right;
                }
                String op = ie.getOp();
                return evalInfixExpression(op, left, right);
            }
            case IfExpression:
                return evalIfExpression((IfExpression) node, env);
            case WhileExpression:
                return evalWhileExpression((WhileExpression) node, env);
            case ForExpression:
                return evalForExpression((ForExpression) node, env);
            case FunctionLiteral:
            {
                FunctionLiteral fl = (FunctionLiteral) node;
                Function function = new Function(env, fl.getBody(),fl.getParameters());
                return function;
            }
            case CallExpression:
            {
                CallExpression ce = (CallExpression) node;
                Obj fn = eval(ce.getFunction(), env);
                if (fn.type() == ObjType.ERROR) {
                    return fn;
                }
                ArrayList<Obj> args = evalExpressionList(ce.getArguments(),env);
                if (args.size() == 1 && args.get(0).type() == ObjType.ERROR) {
                    return args.get(0);
                }
                return evalFunction(fn, args);
            }
            case ArrayLiteral:
            {
                ArrayLiteral al = (ArrayLiteral) node;
                ArrayList<Obj> elements = evalExpressionList(al.getElements(), env);

                if (elements.size() == 1 && elements.get(0).type() == ObjType.ERROR) {
                    return elements.get(0);
                }
                return new Array(elements);
            }
            case IndexExpression:
            {
                IndexExpression ie = (IndexExpression) node;
                Obj left = eval(ie.getLeft(), env);
                if (left.type() == ObjType.ERROR) {
                    return left;
                }
                Obj index = eval(ie.getIndex(), env);
                if (index.type() == ObjType.ERROR) {
                    return index;
                }
                return evalIndexExpression(left, index);
            }
            case HashLiteral:
                return evalHashLiteral((HashLiteral) node, env);
            
            default:
                return new Error("unknown node type: " + node.type());
        }
    }

    private Obj evalAssignExpression(AssignExpression node, Environment env) {
        
        AssignExpression ae = (AssignExpression) node;
        
        Obj val = eval(ae.getRight(), env);
        if (val.type() == ObjType.ERROR) {
            return val;
        }

        Expression ident = ae.getLeft();

        if (ident instanceof Identifier) {
            Identifier id = (Identifier) ident;
            if (env.has(id.getValue())) {
                env.set(id.getValue(), val);
            } else {
                return new Error("identifier not defined: " + id.getValue());
            }
        }
        if (ident instanceof IndexExpression) {
            IndexExpression ie = (IndexExpression) ident;
            Obj left = eval(ie.getLeft(), env);
            if (left.type() == ObjType.ERROR) {
                return left;
            }
            Obj index = eval(ie.getIndex(), env);
            if (index.type() == ObjType.ERROR) {
                return index;
            }
            if (left.type() == ObjType.ARRAY) {
                Array array = (Array) left;
                if (array.length() <= ((Int)index).getValue() 
                    || ((Int)index).getValue() < 0) {
                    return new Error("index out of bounds");
                }
                array.set(((Int)index).getValue(), val);
            }
            if (left.type() == ObjType.HASH) {
                if (index.type() == ObjType.INTEGER) {
                    ((Hash) left).put(((Int)index).hashKey(), new HashPair(index, val));
                }
                if (index.type() == ObjType.STRING) {
                    ((Hash) left).put(((Str)index).hashKey(), new HashPair(index, val));
                }
                if (index.type() == ObjType.BOOLEAN) {
                    ((Hash) left).put(((Bool)index).hashKey(), new HashPair(index, val));
                }
                
            }

            
        }

        return val;
        
    }

    private Obj evalHashLiteral(HashLiteral node, Environment env) {
        HashMap<HashKey, HashPair> pairs = new HashMap<>();
        HashMap<Expression, Expression> pairs2 = node.getPairs();

        for (Expression key : pairs2.keySet()) {
            Obj v = eval(key, env);
            if (v.type() == ObjType.ERROR) {
                return v;
            }
            HashKey hashKey = new HashKey();
            if (v.type() == ObjType.INTEGER) {
                hashKey = ((Int) v).hashKey();
            }
            if (v.type() == ObjType.STRING) {
                hashKey = ((Str) v).hashKey();
            }
            if (v.type() == ObjType.BOOLEAN) {
                hashKey = ((Bool) v).hashKey();
            }
            Obj value = eval(pairs2.get(key), env);
            if (value.type() == ObjType.ERROR) {
                return value;
            }
            pairs.put(hashKey, new HashPair(v, value));
            
        }
        return new Hash(pairs);
    }

    private Obj evalIndexExpression(Obj left, Obj index) {
        if (left.type() == ObjType.ARRAY && index.type() == ObjType.INTEGER) {
            Array array = (Array) left;
            int i = ((Int) index).getValue();
            if (i < 0 || i >= array.getElements().size()) {
                return new Error("index out of bounds");
            }
            return array.getElements().get(i);
        }

        if (left.type() == ObjType.HASH) {
            

            HashKey hashKey = new HashKey();
            if (index.type() == ObjType.STRING) {
                Str key = (Str) index;
                hashKey = key.hashKey();
            }
            if (index.type() == ObjType.INTEGER) {
                Int key = (Int) index;
                hashKey = key.hashKey();
            }
            if (index.type() == ObjType.BOOLEAN) {
                Bool key = (Bool) index;
                hashKey = key.hashKey();
            }
            Hash hash = (Hash) left;
            Obj value = hash.get(hashKey);
            if (value == null) {
                return new Error("key not found in hash: " + index.inspect());
            }
            return ((HashPair)value).getValue();
        }

        if (left.type() == ObjType.STRING) {
            if (index.type() == ObjType.INTEGER) {
                Str str = (Str) left;
                int i = ((Int) index).getValue();
                if (i < 0 || i >= str.length()) {
                    return new Error("index out of bounds");
                }
                return new Str(String.valueOf(str.getValue().charAt(i)));
            }
        }

        return new Error("index operator not supported for " + left.type() + " with index type of " + index.type());
    }

    private Obj evalFunction(Obj fn, ArrayList<Obj> args) {
        if (fn.type() == ObjType.FUNCTION) {
            Function function = (Function) fn;

            if (function.getParams().size() != args.size()) {
                return new Error("wrong number of arguments : want= " + function.getParams().size() + " got=" + args.size());
            }

            Environment extendedEnv = extendFunctionEnv(function, args);
            Obj evaluated = eval(function.getBody(), extendedEnv);
            if (evaluated.type() == ObjType.RETURNVALUE) {
                return ((ReturnValue)evaluated).getValue();
            }
            return evaluated;
        }
        if (fn.type() == ObjType.BUILTIN) {
            BuiltinFunc builtin = (BuiltinFunc) fn;
            Obj evaluated = builtin.exec(args);   
            return evaluated; 
        }
        return new Error("not a function: " + fn.type());
    }

    private ArrayList<Obj> evalExpressionList(ArrayList<Expression> args, Environment env) {
        ArrayList<Obj> objs = new ArrayList<>();
        if (args == null) {
            return objs;
        }
        for (Expression exp : args) {
            Obj obj = eval(exp, env);

            if (obj.type() == ObjType.ERROR) {
                objs.add(obj);
                return objs;
            }
            objs.add(obj);
        }
        return objs;
    }

    private Environment extendFunctionEnv(Function fn, ArrayList<Obj> args) {
        Environment env = new Environment(fn.getEnv());
        for (int i = 0; i < fn.getParams().size(); i++) {
            env.define(fn.getParams().get(i).getValue(), args.get(i));
        }
        return env;
    }

    private Obj evalReturnStatement(ReturnStatement rs, Environment env) {
        Obj val = eval(rs.getRet(), env);
        if (val.type() == ObjType.ERROR) {
            return val;
        }
        return new ReturnValue(val);
    }

    private Obj evalProgram(Program program, Environment env) {
        Obj res = new NULL();

        for (Statement statement : program.getStatements()) {
            res = eval(statement, env);
            if (res.type() == ObjType.RETURNVALUE) {
                return ((ReturnValue) res).getValue();
            }

            if (res.type() == ObjType.ERROR) {
                return res;
            }
        }

        return res;
    }

    private Obj evalBlockStatement(BlockStatement block, Environment env) {
        Obj res = new NULL();
        for (Statement statement : block.getStatements()) {
            res = eval(statement, env);

            if (res == null) {
                return new NULL();
            }

            if (res.type() == ObjType.RETURNVALUE
                    || res.type() == ObjType.ERROR) {
                return res;
            }
        }
        return res;
    }

    private Obj evalPrefixExpression(String operator, Obj right) {
        if (operator.equals("-")) {
            if (right.type() == ObjType.INTEGER) {
                return new Int(-((Int) right).getValue());
            }
            if (right.type() == ObjType.ERROR) {
                return new Error("unknown operator: " + operator + " for " +  right.type());
            }
        }

        if (operator.equals("!")) {
            if (right.type() == ObjType.BOOLEAN) {
                return new Bool(!((Bool) right).getValue());
            }
            if (right.type() == ObjType.NULL) {
                return new Bool(true);
            }
            if (right.type() == ObjType.ERROR) {
                return new Error("unknown operator: " + operator + " for " +  right.type());
            }

            return new Bool(false);
        }

        return new Error("unknown operator: " + operator + " for " +  right.type());
    }

    private Obj evalInfixExpression(String operator, Obj left, Obj right) {
        if (left.type() == ObjType.INTEGER && right.type() == ObjType.INTEGER) {
            return evalIntegerInfixExpression(operator, (Int) left, (Int) right);
        }
        if (left.type() == ObjType.STRING && right.type() == ObjType.STRING) {
            return evalStringInfixExpression(operator, (Str) left, (Str) right);
        }

        if (left.type() == ObjType.BOOLEAN && right.type() == ObjType.BOOLEAN) {
            if (operator.equals("==")) {
                Bool leftBool = (Bool) left;
                Bool rightBool = (Bool) right;
                return new Bool(leftBool.getValue() == rightBool.getValue());
            }
            if (operator.equals("!=")) {
                Bool leftBool = (Bool) left;
                Bool rightBool = (Bool) right;
                return new Bool(leftBool.getValue() != rightBool.getValue());
            }
        }

        return new Error("unknown operator: " + operator +" for "+  left.type() + " and " + right.type());
    }

    private Obj evalIntegerInfixExpression(String operator, Int left, Int right) {
        if (operator.equals("+")) {
            return new Int(left.getValue() + right.getValue());
        }
        if (operator.equals("-")) {
            return new Int(left.getValue() - right.getValue());
        }
        if (operator.equals("*")) {
            return new Int(left.getValue() * right.getValue());
        }
        if (operator.equals("/")) {
            return new Int(left.getValue() / right.getValue());
        }
        if (operator.equals("<")) {
            return new Bool(left.getValue() < right.getValue());
        }
        if (operator.equals(">=")) {
            return new Bool(left.getValue() >= right.getValue());
        }
        if (operator.equals("<=")) {
            return new Bool(left.getValue() <= right.getValue());
        }
        if (operator.equals(">")) {
            return new Bool(left.getValue() > right.getValue());
        }
        if (operator.equals("==")) {
            return new Bool(left.getValue() == right.getValue());
        }
        if (operator.equals("!=")) {
            return new Bool(left.getValue() != right.getValue());
        }

        return new Error("unknown operator: " + operator +" for "+  left.type() + " and " + right.type());
    }

    private Obj evalStringInfixExpression(String operator, Str left, Str right) {
        if (operator.equals("+")) {
            return new Str(left.getValue() + right.getValue());
        }
        if (operator.equals("==")) {
            return new Bool(left.getValue().equals(right.getValue()));
        }
        if (operator.equals("!=")) {
            return new Bool(!left.getValue().equals(right.getValue()));
        }

        return new Error("unknown operator: " + operator +" for "+  left.type() + " and " + right.type());
    }

    private Obj evalForExpression(ForExpression fe, Environment env) {

        BlockStatement body = fe.getBody();
        Statement init = fe.getInit();
        Expression condition = fe.getCondition();
        Statement increment = fe.getIncrement();
        Obj res = new NULL();
        Environment extendedEnv = new Environment(env);

        if (init != null) {
            res = eval(init, extendedEnv);
            if (res.type() == ObjType.ERROR) {
                return res;
            }
        }

        Obj cond = eval(condition, extendedEnv);
        if (cond.type() == ObjType.ERROR) {
            return cond;
        }

        while (cond.type() == ObjType.BOOLEAN && ((Bool)cond).getValue() 
            || cond.type() == ObjType.NULL) {
            boolean hasBreak = false;

            for (Statement statement : body.getStatements()) {
                res = eval(statement, extendedEnv);
                if (res.type() == ObjType.BREAK) {
                    hasBreak = true;
                    break;
                }

                if (res.type() == ObjType.CONTINUE) {
                    break;
                }
            }

            if (hasBreak) {
                break;
            }

            if (increment != null) {
                eval(increment,extendedEnv);
            }
            
            cond = eval(condition, extendedEnv);
            if (cond.type() == ObjType.ERROR) {
                return cond;
            }

            
        }

        return res;
    }

    private Obj evalWhileExpression(WhileExpression we, Environment env) {
        
        BlockStatement body = we.getBody();
        Obj res = new NULL();
        Obj condition = eval(we.getCondition(), env);
        if (condition.type() == ObjType.ERROR) {
            return condition;
        }

        while (condition.type() == ObjType.BOOLEAN && ((Bool) condition).getValue()) {
            
            boolean hasBreak = false;

            for (Statement statement : body.getStatements()) {
                res = eval(statement, env);
                if (res.type() == ObjType.BREAK) {
                    hasBreak = true;
                    break;
                }

                if (res.type() == ObjType.CONTINUE) {
                    break;
                }
            }

            if (hasBreak) {
                break;
            }

            condition = eval(we.getCondition(), env);
            if (condition.type() == ObjType.ERROR) {
                return condition;
            }
        }

        return res;
    }

    private Obj evalIfExpression(IfExpression ie, Environment env) {
        Obj condition = eval(ie.getCondition(),env);
        Statement consequence = ie.getConsequence();
        Statement alternative = ie.getAlternative();
        if (condition.type() == ObjType.ERROR) {
            return condition;
        }

        if (condition.type() == ObjType.BOOLEAN) {
            if (((Bool) condition).getValue()) {
                return eval(consequence, env);
            } 
            if (alternative != null) {
                return eval(alternative, env);
            }

            return new NULL();
        }
        return new Error("unknown error in IfExpression: " + condition.type());
    }

    private Obj evalIdentifier(Identifier id, Environment env) {
        Obj val = env.get(id.getValue());
        if (val != null) {
            return val;
        }

        BuiltinFunc anyFunc = builtin.get(id.getValue());
        if (anyFunc != null) {
            return anyFunc;
        }
        return new Error("identifier not defined: " + id.getValue());
    }

}


