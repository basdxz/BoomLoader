package com.basdxz.boomload.asm;

import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import static com.basdxz.boomload.asm.Exclusions.EXCLUDED_PACKAGES;

@NoArgsConstructor
public final class OpenGLFunctionDumper implements IClassNodeTransformer {
    private static final Map<String, Map<GLFunction, Integer>> GL_FUNCTION_COUNTER = new HashMap<>();
    private static final Map<String, List<GLFunctionUsage>> GL_FUNCTION_USAGES = new HashMap<>();

    private static final Object lock = new Object();

    @Override
    public String getName() {
        return "OpenGL Function Dumper";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        if (isExcluded(cn))
            return false;

        for (val meth : cn.methods) {
            meth.accept(new MethodVisitor(Opcodes.ASM5) {
                private int lineNumber = -1;

                @Override
                public void visitLineNumber(int line, Label start) {
                    lineNumber = line;
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    if (isOpenGLFunction(owner))
                        addOpenGLFunction(cn.name, owner, name, desc, lineNumber);
                }
            });
        }

        return false;
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {}

    private static boolean isExcluded(ClassNode cn) {
        if (cn == null)
            return true;
        val name = cn.name;
        if (name == null)
            return true;

        for (val packageName : EXCLUDED_PACKAGES) {
            if (name.startsWith(packageName + "."))
                return false;
        }

        return false;
    }

    private static boolean isOpenGLFunction(String owner) {
        return owner != null && owner.startsWith("org/lwjgl/opengl/");
    }

    private static void addOpenGLFunction(String clazz, String owner, String name, String desc, int lineNumber) {
        synchronized (lock) {
            val func = new GLFunction(owner, name, desc);
            val functions = GL_FUNCTION_COUNTER.computeIfAbsent(func.spec, s -> new HashMap<>());
            functions.put(func, functions.getOrDefault(func, 0) + 1);

            val funcUse = new GLFunctionUsage(lineNumber, func);
            val functionUses = GL_FUNCTION_USAGES.computeIfAbsent(clazz, s -> new ArrayList<>());
            functionUses.add(funcUse);
        }
    }

    @SneakyThrows
    public static void dumpOpenGLFunctions() {
        val outputStream = new FileOutputStream(FileUtil.getMinecraftHome().toPath().resolve("ogl_use_dump.txt").toFile());
        val printWriter = new PrintWriter(outputStream);

        printWriter.println("--GL FUNCTIONS BY SPEC--");
        for (val value : GL_FUNCTION_COUNTER.entrySet()) {
            printWriter.println("spec: " + value.getKey());

            for (val value2 : value.getValue().entrySet())
                printWriter.println("    " + value2.getKey() + "[" + value2.getValue() + "]");

            printWriter.println("--");
        }

        printWriter.println("\n\n");

        printWriter.println("--GL FUNCTIONS BY CLASS--");
        for (val value : GL_FUNCTION_USAGES.entrySet()) {
            printWriter.println("class: " + value.getKey());

            for (val value2 : value.getValue())
                printWriter.println("    " + value2);

            printWriter.println("--");
        }

        outputStream.close();
    }

    private static class GLFunction {
        private final String spec;
        private final String func;

        private GLFunction(String owner, String name, String desc) {
            spec = owner.replace("org/lwjgl/opengl/", "");
            func = name + desc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(spec, func);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GLFunction))
                return false;
            val other = (GLFunction) o;
            return spec.equals(other.spec) && func.equals(other.func);
        }

        @Override
        public String toString() {
            return spec + "#" + func;
        }
    }

    @AllArgsConstructor
    private static class GLFunctionUsage implements Comparable<GLFunctionUsage> {
        private final int line;
        private final GLFunction func;

        @Override
        public int compareTo(@NotNull OpenGLFunctionDumper.GLFunctionUsage o) {
            return Integer.compare(line, o.line);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GLFunctionUsage))
                return false;
            val other = (GLFunctionUsage) o;
            return line == other.line && func.equals(other.func);
        }

        @Override
        public String toString() {
            return line + ":" + func.toString();
        }
    }
}
