package core.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import core.ast.Program;
import core.ast.base.ASTNode;
import core.ast.base.ASTNodeType;
import core.ast.expression.ArrayLiteral;
import core.ast.expression.AssignExpression;
import core.ast.expression.BooleanLiteral;
import core.ast.expression.CallExpression;
import core.ast.expression.Expression;
import core.ast.expression.ForExpression;
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
import core.ast.statement.BreakStatement;
import core.ast.statement.ContinueStatement;
import core.ast.statement.ExpressionStatement;
import core.ast.statement.ReturnStatement;
import core.ast.statement.VarStatement;
import core.compiler.utils.BreakContinueTable;
import core.compiler.utils.CompileScope;
import core.compiler.utils.Symbol;
import core.compiler.utils.SymbolTable;
import core.env.CompiledFunction;
import core.env.Int;
import core.env.Obj;
import core.env.Str;
import core.is.InstructionSet;
import core.is.Opcode;

public class Compiler {
    private ArrayList<Obj> consts = new ArrayList<>();
    private Stack<CompileScope> scopes = new Stack<>();

    private InstructionSet is = new InstructionSet();
    private SymbolTable st = new SymbolTable();

    private BreakContinueTable bct = null;

    private ArrayList<String> errors = new ArrayList<>();

    public Compiler() {
        scopes.push(new CompileScope(0));
        gen(Opcode.Allocate, 0);
    }

    public CompiledProgram compile(ASTNode ast) {
        int constIndex;
        Obj tempObj;
        switch (ast.type()) {
            case Program:
                Program program = (Program) ast;
                for (int i = 0; i < program.getStatements().size(); i++) {
                    compile(program.getStatements().get(i));
                }
                replace(0, 3, is.encode(Opcode.Allocate, st.size()));
                break;
            case BlockStatement:
                BlockStatement bs = (BlockStatement) ast;
                for (int i = 0; i < bs.getStatements().size(); i++) {
                    compile(bs.getStatements().get(i));
                }
                break;
            case ExpressionStatement:
                ExpressionStatement es = (ExpressionStatement) ast;
                compile(es.getExpression());
                gen(Opcode.Pop);
                break;
            case VarStatement:
                VarStatement vs = (VarStatement) ast;
                String idname = vs.getIdentifier().getValue();
                Symbol s = st.define(idname);
                compile(vs.getValue());
                
                if (s.scope().equals("global")) {
                    gen(Opcode.SetGlobal, s.index());
                }
                if (s.scope().equals("local")) {
                    gen(Opcode.SetLocal, s.index());
                }
                
                break;
            case BreakStatement:
                BreakStatement brs = (BreakStatement) ast;
                genBreak(brs);
                break;
            case ContinueStatement:
                ContinueStatement cts = (ContinueStatement) ast;
                genContinue(cts);
                break;
            case ReturnStatement:
                ReturnStatement rs = (ReturnStatement) ast;
                compile(rs.getRet());
                gen(Opcode.ReturnValue);
                break;
            case IntegerLiteral:
                IntegerLiteral il = (IntegerLiteral) ast;
                tempObj = new Int(il.getValue());
                constIndex = addConstant(tempObj);
                gen(Opcode.Load, constIndex);
                break;
            case BooleanLiteral:
                BooleanLiteral bl = (BooleanLiteral) ast;
                if (bl.getValue()) {
                    gen(Opcode.True);
                } else {
                    gen(Opcode.False);
                }
                break;
            case StringLiteral:
                StringLiteral  sl = (StringLiteral) ast;
                tempObj = new Str(sl.getValue());
                constIndex = addConstant(tempObj);
                gen(Opcode.Load, constIndex);
                break;
            case ArrayLiteral:
                ArrayLiteral al = (ArrayLiteral) ast;
                ArrayList<Expression> elements = al.getElements();
                for (int i = elements.size() - 1; i >= 0 ; i--) {
                    compile(elements.get(i));
                }
                gen(Opcode.Array, elements.size());
                break;
            case HashLiteral:
                HashLiteral hl = (HashLiteral) ast;
                HashMap<Expression,Expression> hm = hl.getPairs();
                for (Expression exp : hm.keySet()) {
                    compile(hm.get(exp));
                    compile(exp);
                }
                gen(Opcode.Hash, hm.size());
                break;

            case InfixExpression:
                InfixExpression ie = (InfixExpression) ast;
                compile(ie.getRight());
                compile(ie.getLeft());
                genBinaryOperator(ie.getOp());
                break; 
            case PrefixExpression:
                PrefixExpression pe = (PrefixExpression) ast;
                compile(pe.getRight());    
                genSingleOperator(pe.getOp());
                break;
            case IndexExpression:
                IndexExpression ide = (IndexExpression) ast;
                compile(ide.getLeft());
                compile(ide.getIndex());
                gen(Opcode.Index);
                break;
            case IfExpression:
                IfExpression ife = (IfExpression) ast; 
                genIf(ife);
                break;
            case AssignExpression:
                AssignExpression ae = (AssignExpression) ast;
                genAssign(ae);
                
                break;
            case WhileExpression:
                WhileExpression we = (WhileExpression) ast;
                genWhile(we);
                break;
            case ForExpression:
                ForExpression fe = (ForExpression) ast;
                genFor(fe);
                break;
            case CallExpression:
                CallExpression ce = (CallExpression) ast;
                genCall(ce);
                break;
            case Identifier:
                Identifier id = (Identifier) ast;
                loadVars(id.getValue());
                break;
            case FunctionLiteral:
                FunctionLiteral fl = (FunctionLiteral) ast;
                genFunction(fl);
                break;
            default:
                break;
        }

        return new CompiledProgram(curInstructions(), consts, errors);
    }

    private void replaceLastPopWithReturn() {
        boolean removed = curScope().removeLastPop();
        if (removed) {
            gen(Opcode.ReturnValue);
        }
    }

    private void addError(String msg) {
        errors.add(msg);
    }


    private void enterNewScope(int params) {
        scopes.push(new CompileScope(params));
        st = new SymbolTable(st);
    }

    private CompiledFunction leaveScope() {
        CompileScope scope = scopes.pop();
        st = st.outer();
        CompiledFunction cf = new CompiledFunction(scope.instructions(), scope.params());
        return cf;
    }

    CompileScope curScope() {
        return scopes.peek();
    }

    private ArrayList<Byte> curInstructions() {
        return scopes.peek().instructions();
    }

    private void loadVars(String name) {
        Symbol s = st.get(name);

        if (s == null) {
            addError("错误: 变量未定义: " + name);
            return;
        }

        if (s.scope().equals("global")) {
            gen(Opcode.GetGlobal, s.index());
        } 
        if (s.scope().equals("local")) {
            gen(Opcode.GetLocal, s.index());
        }
        if (s.scope().equals("builtin")) {
            gen(Opcode.GetBuiltin, s.index());
        }
        if (s.scope().equals("free")) {
            gen(Opcode.GetFree, s.index());
        }
        if (s.scope().equals("function")) {
            gen(Opcode.CurClosure);
        }
    }

    private void replace(int start, int len, ArrayList<Byte> bytes) {
        if (len != bytes.size()) {
            System.out.println("len != bytes.size()");
            return;
        }
        for (int i = 0; i < len; i++) {
            curInstructions().set(start + i, bytes.get(i));
        }
    }

    private int addConstant(Obj obj) {
        consts.add(obj);
        return consts.size() - 1;
    }


    private int gen(Opcode code, int ... args) {
        int index = curInstructions().size();

        curScope().setLast(code, index);

        curInstructions().addAll(is.encode(code, args));
        return index;
    }

    private void genAssign(AssignExpression ae) {
        compile(ae.getRight());
        if (ae.getLeft().type() == ASTNodeType.Identifier) {
            Identifier id = (Identifier) ae.getLeft();
            Symbol s = st.get(id.getValue());

            if (s == null) {
                addError("错误： 变量未定义: " + id.getValue());
                return;
            }

            if (s.scope().equals("global")) {
                gen(Opcode.SetGlobal, s.index());
                gen(Opcode.GetGlobal, s.index());
                return;
            }
            if (s.scope().equals("local")) {
                gen(Opcode.SetLocal, s.index());
                gen(Opcode.GetLocal, s.index());
                return;
    
            }
            if (s.scope().equals("free")) {
                gen(Opcode.SetFree, s.index());
                gen(Opcode.GetFree, s.index());
                return;
            }

        }
        if (ae.getLeft().type() == ASTNodeType.IndexExpression) {
            IndexExpression ie = (IndexExpression) ae.getLeft();
            compile(ie.getLeft());
            Identifier id = (Identifier) ie.getLeft();
            compile(ie.getIndex());
            Symbol s = st.get(id.getValue());

            if (s == null) {
                addError("错误： 变量未定义: " + id.getValue());
                return;
            }

            gen(Opcode.SetArray, null);
            gen(Opcode.SetLocal, s.index());
            gen(Opcode.GetLocal, s.index());

            return;
        }

        addError("错误： 左值不支持赋值 ： " + ae.getLeft().toString());

    }

    private void genCall(CallExpression ce) {
        compile(ce.getFunction());
        for (int i = 0; i < ce.getArguments().size(); i++) {
            compile(ce.getArguments().get(i));
        }
        gen(Opcode.Call, ce.getArguments().size());
    }

    private void genFunction(FunctionLiteral fl) {
        enterNewScope(fl.getParameters().size());
        
        if (fl.getName() != null) {
            st.defineFunction(fl.getName());
        }

        int aindex = gen(Opcode.Allocate, 0);

        for (int i = 0; i < fl.getParameters().size(); i++) {
            st.define(fl.getParameters().get(i).getValue());
        }

        compile(fl.getBody());

        replaceLastPopWithReturn();

        if (!curScope().lastIs(Opcode.ReturnValue)) {
            gen(Opcode.Return);
        }

        replace(aindex, 3, is.encode(Opcode.Allocate, st.size() - fl.getParameters().size()));

        ArrayList<Symbol> frees = st.frees();

        CompiledFunction cf = leaveScope();

        for (int i = 0; i < frees.size(); i++) {
            loadVars(frees.get(i).name());
        }

        int index = addConstant(cf);
        gen(Opcode.Closure, index, frees.size()); 
    }

    private void genBreak(BreakStatement brs) {
        gen(Opcode.NULL, null);
        bct.addBreakPoint(curInstructions().size());
        gen(Opcode.J, 0);
        
    }

    private void genContinue(ContinueStatement cts) {
        gen(Opcode.J, 0);
        bct.addContinuePoint(curInstructions().size());
        gen(Opcode.NULL, null);
        
        

    }

    private void genFor(ForExpression fe) {
        bct = new BreakContinueTable(bct);

        compile(fe.getInit());
        int start = curInstructions().size();
        compile(fe.getCondition());
        int jfindex = gen(Opcode.Jf, 0);
        compile(fe.getBody());
        int incindex = curInstructions().size();
        compile(fe.getIncrement());
        int jindex = gen(Opcode.J, start);
        replace(jfindex, 3, is.encode(Opcode.Jf, curInstructions().size() - jfindex - 3));
        replace(jindex, 3,  is.encode(Opcode.J,   start - jindex - 3));
        gen(Opcode.NULL, null);
        ArrayList<Integer> breakPoints = bct.getBreakPoints();
        ArrayList<Integer> continuePoints = bct.getContinuePoints();

        for (int i = 0; i < breakPoints.size(); i++) {
            replace(breakPoints.get(i), 3, is.encode(Opcode.J, curInstructions().size() - breakPoints.get(i) - 3));
        }
        for (int i = 0; i < continuePoints.size(); i++) {
            replace(continuePoints.get(i), 3, is.encode(Opcode.J, incindex - continuePoints.get(i) - 3));
        } 

        bct = bct.outer();
    }

    private void genWhile(WhileExpression we) {
        bct = new BreakContinueTable(bct);

        int startPos = curInstructions().size();
        compile(we.getCondition());
        int jfindex = gen(Opcode.Jf, 0);
        compile(we.getBody());
        int jindex = gen(Opcode.J, startPos);
        replace(jfindex, 3, is.encode(Opcode.Jf, curInstructions().size() - jfindex - 3));
        replace(jindex, 3,  is.encode(Opcode.J,   startPos - jindex - 3));
        gen(Opcode.NULL, null);
        int endPos = curInstructions().size();
        ArrayList<Integer> breakPoints = bct.getBreakPoints();
        ArrayList<Integer> continuePoints = bct.getContinuePoints();

        for (int i = 0; i < breakPoints.size(); i++) {
            replace(breakPoints.get(i), 3, is.encode(Opcode.J, endPos - breakPoints.get(i) - 3));
        }

        for (int i = 0; i < continuePoints.size(); i++) {
            replace(continuePoints.get(i), 3, is.encode(Opcode.J, startPos - continuePoints.get(i) - 3));
        }

        bct = bct.outer();

    }

    private void genIf(IfExpression ife) {
        compile(ife.getCondition());
        int jfindex = gen(Opcode.Jf, 0);
        compile(ife.getConsequence());

        if (ife.getConsequence().type() == ASTNodeType.BlockStatement) {
            BlockStatement bs = (BlockStatement) ife.getConsequence();
            if (bs.getStatements().size() == 0) {
                gen(Opcode.NULL, 0);
            }   
        }
        
        curScope().removeLastPop();
        int jindex = gen(Opcode.J, 0);
        replace(jfindex, 3, is.encode(Opcode.Jf, curInstructions().size() - jfindex - 3));
        if(ife.getAlternative() != null)
        {
            
            compile(ife.getAlternative());

            if (ife.getAlternative().type() == ASTNodeType.BlockStatement) {
                BlockStatement bs = (BlockStatement) ife.getAlternative();
                if (bs.getStatements().size() == 0) {
                    gen(Opcode.NULL, 0);
                }   
            }
            curScope().removeLastPop();

        } else 
        {
            gen(Opcode.NULL, 0);
        }
        replace(jindex, 3, is.encode(Opcode.J, curInstructions().size() - jindex - 3));
    }

    private void genBinaryOperator(String op) {
        switch (op) {
            case "+":
                gen(Opcode.Add);
                break;
            case "-":
                gen(Opcode.Sub);
                break;
            case "*":
                gen(Opcode.Mul);
                break;
            case "/":
                gen(Opcode.Div);
                break;
            case "==":
                gen(Opcode.Eq);
                break;
            case "!=":
                gen(Opcode.Ne);
                break;
            case ">":
                gen(Opcode.Gt);
                break;
            case ">=":
                gen(Opcode.Ge);
                break;
            case "<":
                gen(Opcode.Lt);
                break;
            case "<=":
                gen(Opcode.Le);
                break;
            default:
                addError("不支持的操作符: " + op);
                break;
        }


    }

    private void genSingleOperator(String op) {
        switch (op) {
            case "-":
                gen(Opcode.Neg);
                break;
            case "!":
                gen(Opcode.Not);
                break;
            default:
                addError("不支持的操作符: " + op);
                break;
        }
    }




}
