import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import core.ast.Program;
import core.compiler.CompiledProgram;
import core.compiler.Compiler;
import core.env.CompiledFunction;
import core.env.Environment;
import core.env.NULL;
import core.env.Obj;
import core.env.ObjType;
import core.eval.Evaluator;
import core.lexer.Lexer;
import core.is.CompiledProgramReaderWriter;
import core.is.InstructionSet;

import core.parser.Parser;
import core.vm.Vm;

public class Main {

    public static void main(String[] args) { 
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            System.out.print(">>> ");
            String line = scanner.nextLine();
            if (line.equals("exit")) { break; }

            if (line.startsWith("exec ")) {
                line = line.substring(5);
                if (line.length() == 0) {
                    System.out.println("No file input");
                    continue;
                }
                runFile(line);
                continue;
            }

            if (line.startsWith("help")) {
                System.out.println("exec <file> - 执行文件（解释器模式）\n" + 
                "run <file> - 执行二进制文件（虚拟机模式）\n" + 
                "compile <src> [dst] - 编译文件\n" + 
                "deasm <file> - 反汇编二进制文件\n" + 
                "build <src> - 编译文件并执行\n");
                continue;
            }

            if (line.startsWith("compile ")) {
                String[] files = line.split(" ");
                if (files.length == 1) {
                    System.out.println("No file input");
                    continue;
                }
                if (files.length == 2) {
                    compileToFile(files[1], files[1].substring(0, files[1].lastIndexOf(".")) + ".cho");
                    continue;
                }
                compileToFile(files[1], files[2]);
                continue;
            }

            if (line.startsWith("deasm ")) {
                String[] files = line.split(" ");
                if (files.length == 1) {
                    System.out.println("No file input");
                    continue;
                }
                deAssemble(files[1]);
                continue;
            }

            if (line.startsWith("run ")) {
                String[] files = line.split(" ");
                if (files.length == 1) {
                    System.out.println("No file input");
                    continue;
                }
                runExecutable(files[1]);
                continue;
            }

            if (line.startsWith("build ")) {
                String[] files = line.split(" ");
                if (files.length == 1) {
                    System.out.println("No file input");
                    continue;
                }
                compileAndRun(files[1]);
                continue;
            }

        }

        scanner.close();


    }

    private static void deAssemble(String path) {
        CompiledProgram compiledProgram = CompiledProgramReaderWriter.read(path);
        printInfo(compiledProgram);
    }

    private static void runExecutable(String path) {
        CompiledProgram compiledProgram = CompiledProgramReaderWriter.read(path);
        Vm vm = new Vm(compiledProgram);
        Obj obj = vm.run();
        System.out.println("return:" +obj.inspect());
    }

    private static void compileToFile(String src, String dst) {
        String str = "";
        try {
            str = Files.readString(Path.of(src));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Lexer lexer = new Lexer(str);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        if (parser.getErrors().size() > 0) {
            for (String error : parser.getErrors()) {
                System.out.println(error);
            }
            return;
        }
        Compiler compiler = new Compiler();
        CompiledProgram compiledProgram = compiler.compile(program);

        if (compiledProgram.errors().size() > 0) {
            for (String error : compiledProgram.errors()) {
                System.out.println(error);
            }
            return;
        }

        try {
            CompiledProgramReaderWriter.write(compiledProgram, dst);
        } catch (IOException e) {
            System.out.println("write file error: " + e.getMessage());
        }
    }

    private static void compileAndRun(String path) {
        String str = "";
        try {
            str = Files.readString(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Lexer lexer = new Lexer(str);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        if (parser.getErrors().size() > 0) {
            for (String error : parser.getErrors()) {
                System.out.println(error);
            }
            return;
        }
        Compiler compiler = new Compiler();
        CompiledProgram compiledProgram = compiler.compile(program);

        if (compiledProgram.errors().size() > 0) {
            for (String error : compiledProgram.errors()) {
                System.out.println(error);
            }
            return;
        }
        Vm vm = new Vm(compiledProgram);
        Obj obj = vm.run();
        
        System.out.println("return:" +obj.inspect());
    }

    private static void printInfo(CompiledProgram compiledProgram) {
        System.out.println("\nInstructions:");

        InstructionSet is = new InstructionSet();
        ArrayList<String> instructions = is.decode(compiledProgram.instructions());
        ArrayList<Obj> consts = compiledProgram.consts();
        for (String instruction : instructions) {
            System.out.println(instruction);
        }
        System.out.println("\nConstants:");
        int i = 0;
        for (Obj obj : consts) {
            System.out.print("%03d: ".formatted(i++));
            if (obj.type() == ObjType.CompiledFunction) {
                CompiledFunction cf = (CompiledFunction) obj;
                instructions = is.decode(cf.getInstructions());
                System.out.println();
                for (String instruction : instructions) {
                    System.out.println("\t" + instruction);
                }
            }else if (obj.type() == ObjType.STRING) {
                System.out.println("\"" + obj.inspect() + "\"");
            } else {
                System.out.println(obj.inspect());
            }
            
        }

    }

    /**
     * 执行文件内的代码，并输出结果
     * @param path 文件路径
     */
    private static void runFile(String path) {
        String str = "";
        try {
            str = Files.readString(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Lexer lexer = new Lexer(str);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        if (parser.getErrors().size() > 0) {
            for (String error : parser.getErrors()) {
                System.out.println(error);
            }
            return;
        }
        Environment env = new Environment();
        Evaluator evaluator = new Evaluator();

        Obj obj = evaluator.eval(program, env);

        System.out.println("Program finished with result:" + obj.inspect());
    }

    /**
     *  读取控制台输入，并执行
     */
    private static void repl() {
        Environment env = new Environment();
        Evaluator evaluator = new Evaluator();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        Obj obj = new NULL();
        while (true) {
            System.out.print(">>> ");
            String line = scanner.nextLine();

            if (line.equals("exit")) { break; }

            if (line.startsWith("exec ")) {
                line = line.substring(5);
                if (line.length() == 0) {
                    System.out.println("No file input");
                    continue;
                }
                runFile(line);
                continue;
            }

            Lexer lexer = new Lexer(line);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            if (new Parser(new Lexer(line)).getErrors().size() > 0) {
                for (String error : new Parser(new Lexer(line)).getErrors()) {
                    System.out.println(error);
                }
                continue;
            }
            obj = evaluator.eval(program, env);
            System.out.println();
            System.out.println("return value:" + obj.inspect());

        }
        System.out.println("Program finished with result:" + obj.inspect());
        scanner.close();
    }

    private static void printInstr(ArrayList<Byte> instr) {
        for (int i = 0; i < instr.size(); i++) {
            System.out.print("%02x ".formatted(instr.get(i)));
        }
        System.out.println();
    }
}
