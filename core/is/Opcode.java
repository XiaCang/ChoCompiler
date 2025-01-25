package core.is;

public enum Opcode {
    Nop,
    NULL,
    Load,
    True,
    False,
    Add,
    Sub,
    Mul,
    Div,
    Neg,
    Not,
    Eq,
    Ne,
    Gt,
    Ge,
    Lt,
    Le,
    Array,
    Hash,

    J,
    Jf,

    Index,
    Call,

    ReturnValue,
    Return,
    Assign,
    Allocate,
    
    Pop,
    SetGlobal,
    GetGlobal,
    SetLocal,
    GetLocal,
    GetBuiltin,
    Closure,
    GetFree,
    SetFree,
    CurClosure,
    SetArray
}
