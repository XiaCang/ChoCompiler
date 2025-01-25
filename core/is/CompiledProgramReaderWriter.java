package core.is;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import core.compiler.CompiledProgram;
import core.env.CompiledFunction;
import core.env.Int;
import core.env.Obj;
import core.env.Str;

public class CompiledProgramReaderWriter {



    public static CompiledProgram read(String program) {

        ArrayList<Obj> constants = new ArrayList<>();
        ArrayList<Byte> instructions = new ArrayList<>();

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(program);
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + e.getMessage());
            return null;
        }
        byte[] bytes ;
        try {
            bytes = fileInputStream.readAllBytes();
        } catch (IOException e) {
            System.out.println("read file error: " + e.getMessage());
            try {
                fileInputStream.close();
            } catch (IOException e2) {
                System.out.println("close file error: " + e2.getMessage());
            }
            return null;
        }
        try {
            fileInputStream.close();
        } catch (IOException e) {
            System.out.println("close file error: " + e.getMessage());
        }
        int index = 0;
        int len = bytes.length;

        byte[] filetype = new byte[3];
        filetype[0] = bytes[0];
        filetype[1] = bytes[1];
        filetype[2] = bytes[2];
        if (filetype[0] != 0x43 || filetype[1] != 0x48 || filetype[2] != 0x4F) {
            System.out.println("所选文件不是Cho语言编译后的二进制文件");
            return null;
        }
        index += 3;

        byte[] constsLen = new byte[2];
        constsLen[0] = bytes[3];
        constsLen[1] = bytes[4];
        int constsLenInt = readSignedInt(constsLen);
        index += 2;
        for (int i = 0; i < constsLenInt; i++) {
            byte type = bytes[index];
            index += 1;

            if (type == 0) {
                byte[] intConst = new byte[4];
                intConst[0] = bytes[index];
                intConst[1] = bytes[index + 1];
                intConst[2] = bytes[index + 2];
                intConst[3] = bytes[index + 3];
                int intConstInt = readSignedInt(intConst);
                index += 4;
                constants.add(new Int(intConstInt));
            }
            if (type == 1) {
                String str = "";
                while (bytes[index] != 0) {
                    str += (char) bytes[index];
                    index += 1;
                }
                Str s = new Str(str);
                index += 1;
                constants.add(s);
            }
            if (type == 2) {
                byte[] compiledFunc = new byte[2];
                compiledFunc[0] = bytes[index];
                compiledFunc[1] = bytes[index + 1];
                int compiledFuncLen = readSignedInt(compiledFunc);
                index += 2;

                int parasCount = bytes[index];
                index += 1;

                ArrayList<Byte> compiledFuncBytes = new ArrayList<>();

                for (int j = 0; j < compiledFuncLen; j++) {
                    compiledFuncBytes.add(bytes[index]);
                    index += 1;
                }

                CompiledFunction cf = new CompiledFunction(compiledFuncBytes, parasCount);
                constants.add(cf);
            }
        }

        for (int i = index; i < len; i++) {
            instructions.add(bytes[i]);
        }

        return new CompiledProgram(instructions,constants);
    }

    public static void write(CompiledProgram program, String path) throws IOException {
        ArrayList<Obj> constants = program.consts();
        ArrayList<Byte> instructions = program.instructions();
        FileOutputStream fileOutputStream ;
        try {
            fileOutputStream = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + e.getMessage());
            return;
        }

        int constsNum = constants.size();
        byte[] filetype = new byte[3];
        filetype[0] = (byte) 0x43;
        filetype[1] = (byte) 0x48;
        filetype[2] = (byte) 0x4F;
        fileOutputStream.write(filetype);
        fileOutputStream.write(getBytes(constsNum, 2));
        
        for (int i = 0; i < constsNum; i++) {
            Obj o = constants.get(i);
            if (o instanceof Int) {
                fileOutputStream.write((byte) 0);
                Int intConst = (Int) o;
                fileOutputStream.write(getBytes(intConst.getValue(), 4));
            }
            if (o instanceof Str) {
                fileOutputStream.write((byte) 1);
                Str str = (Str) o;
                fileOutputStream.write(str.getValue().getBytes());
                fileOutputStream.write((byte) 0);
            }
            if (o instanceof CompiledFunction) {
                fileOutputStream.write((byte) 2);
                CompiledFunction cf = (CompiledFunction) o;
                fileOutputStream.write(getBytes(cf.getInstructions().size(), 2));
                fileOutputStream.write(getBytes(cf.paramCount(), 1));
                for (int j = 0; j < cf.getInstructions().size(); j++) {
                    fileOutputStream.write(cf.getInstructions().get(j));
                }
            }
        }
        for (int i = 0; i < instructions.size(); i++) {
            fileOutputStream.write(instructions.get(i).byteValue());
        }

        fileOutputStream.close();

    }
    private static byte[] getBytes(int num, int len) {
        byte[]  bytes = new byte[len];
        int j = 0;
        for (int i = len - 1; i >= 0; i--) {
            bytes[j++] = ((byte) (num >> (i * 8)));
        }
        return bytes;
    }

    private static int readSignedInt(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        int result = 0;

        for (int i = 0; i < bytes.length; i++) {
            
            result = (result << 8) | (bytes[i] & 0xFF);
        }

        if ((result & (1 << (bytes.length * 8 - 1))) != 0) {
            result -= (1 << (bytes.length * 8));
        }

        return result;
    }
}
