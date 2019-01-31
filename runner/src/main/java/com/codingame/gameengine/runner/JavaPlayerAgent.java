package com.codingame.gameengine.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Properties;

import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Loader;

/**
 * A participating AI written as a Java class.
 */
public class JavaPlayerAgent extends Agent {
    private JavaAgentThread javaRunnerThread = null;
    private String codeMain = "Player";

    private PipedInputStream agentStdin = new PipedInputStream(100_000);
    private PipedOutputStream agentStdout = new PipedOutputStream();
    private PipedOutputStream agentStderr = new PipedOutputStream();

    private OutputStream processStdin;
    private InputStream processStdout;
    private InputStream processStderr;

    /**
     * @param className
     *            The name of the class to use as a participating AI.
     */
    public JavaPlayerAgent(String className) {
        super();

        codeMain = className;

        try {
            processStdin = new PipedOutputStream(agentStdin);
            processStdout = new PipedInputStream(agentStdout, 100_000);
            processStderr = new PipedInputStream(agentStderr, 100_000);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize Player Agent", e);
        }
    }

    @Override
    protected OutputStream getInputStream() {
        return processStdin;
    }

    @Override
    protected InputStream getOutputStream() {
        return processStdout;
    }

    @Override
    protected InputStream getErrorStream() {
        return processStderr;
    }

    @Override
    public void initialize(Properties conf) {
    }

    /**
     * Launch the agent. After the call, agent is ready to process input / output
     * 
     * @throws Exception
     *             if an error occurs
     */
    @Override
    protected void runInputOutput() throws Exception {
        javaRunnerThread = new JavaAgentThread(codeMain, agentStdin, agentStdout, agentStderr);
        javaRunnerThread.start();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void destroy() {
        if (javaRunnerThread != null) {
            javaRunnerThread.setStopping(true);
            javaRunnerThread.interrupt();
            try {
                javaRunnerThread.join(100);
            } catch (InterruptedException ie) {
                // TODO
            }
            if (javaRunnerThread.isAlive()) {
                // TODO
                javaRunnerThread.interrupt();
//                 javaRunnerThread.destroy();
                javaRunnerThread.stop();
            }
        }
    }

    static class JavaAgentThread extends Thread {
        InputStream stdin;
        PrintStream stdout;
        PrintStream stderr;
        boolean stopping = false;

        private String className;

        public JavaAgentThread(String mainClass, InputStream stdin, OutputStream stdout, OutputStream stderr)
                throws ClassNotFoundException, NoSuchMethodException {
            className = mainClass;
            this.stdin = stdin;
            this.stdout = new PrintStream(stdout);
            this.stderr = new PrintStream(stderr);
        }

        public void setStopping(boolean stopping) {
            this.stopping = stopping;
        }

        private static Class<?> redirectIOs(String className, PrintStream out, PrintStream err, InputStream in) {
            try {
                String newName = "Agent" + (int) (Math.random() * 1_000_000);
                ClassPool pool = ClassPool.getDefault();
                CtClass cc = pool.getAndRename(className, newName);

                cc.setModifiers(cc.getModifiers() | Modifier.PUBLIC);

                try {
                    CtField field = new CtField(pool.get("java.io.PrintStream"), "system_out", cc);
                    field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
                    cc.addField(field);
                } catch (javassist.bytecode.DuplicateMemberException e) {
                    System.err.println(e.getMessage());
                }
                try {
                    CtField field = new CtField(pool.get("java.io.PrintStream"), "system_err", cc);
                    field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
                    cc.addField(field);
                } catch (javassist.bytecode.DuplicateMemberException e) {
                    System.err.println(e.getMessage());
                }
                try {
                    CtField field = new CtField(pool.get("java.io.InputStream"), "system_in", cc);
                    field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
                    cc.addField(field);
                } catch (javassist.bytecode.DuplicateMemberException e) {
                    System.err.println(e.getMessage());
                }

                CodeConverter conv = new CodeConverter();
                conv.redirectFieldAccess(pool.getCtClass("java.lang.System").getField("out"), cc, "system_out");
                conv.redirectFieldAccess(pool.getCtClass("java.lang.System").getField("err"), cc, "system_err");
                conv.redirectFieldAccess(pool.getCtClass("java.lang.System").getField("in"), cc, "system_in");
                for (CtMethod m : cc.getDeclaredMethods()) {
                    m.instrument(conv);
                }
                for (CtConstructor c : cc.getDeclaredConstructors()) {
                    c.instrument(conv);
                }

                Class<?> c = new Loader(pool).loadClass(newName);
                c.getDeclaredField("system_out").set(null, out);
                c.getDeclaredField("system_err").set(null, err);
                c.getDeclaredField("system_in").set(null, in);
                return c;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                try {
                    Constructor<?> c = Class.forName(className).getConstructor(InputStream.class, PrintStream.class,
                            PrintStream.class);
                    c.setAccessible(true);
                    c.newInstance(stdin, stdout, stderr);
                } catch (java.lang.NoSuchMethodException e) {
                    try {
                        Method m = redirectIOs(className, this.stdout, this.stderr, stdin).getMethod("main",
                                String[].class);
                        m.invoke(null, new Object[1]);
                    } catch (java.lang.NoSuchMethodException e2) {
                        System.err.println("No main found for class " + className);
                        System.exit(1);
                    }
                }
            } catch (InvocationTargetException e) {
                Optional.ofNullable(e.getCause()).orElse(e).printStackTrace(stderr);
            } catch (Exception ex) {
                if (!stopping) {
                    System.err.println("Agent failed!");
                    ex.printStackTrace();
                }
                try {
                    stdin.close();
                } catch (IOException ioe) {
                    /* ignore */
                }
                stdout.close();
                stderr.close();
            }
        }
    }
}
