package mobi.mofokom.test;

import static junit.framework.Assert.assertEquals;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.HostAccess.TargetMappingPrecedence;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;
import org.junit.Test;

public class AppTest {

    @Test
    public void testApp() {
        Engine engine = Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", Boolean.toString(false))
                .build();
        ThreadLocal<Context> contextLocal = ThreadLocal.withInitial(() -> Context.newBuilder()
                .engine(engine)
                .allowAllAccess(true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowHostAccess(HostAccess.newBuilder()
                        .allowPublicAccess(true)
                        .allowAllImplementations(true)
                        .allowAllClassImplementations(true)
                        .allowArrayAccess(true)
                        .allowListAccess(true)
                        .targetTypeMapping(Number.class, String.class, n -> true, n -> n.toString(), TargetMappingPrecedence.LOWEST)
                        .targetTypeMapping(Number.class, Long.class, n -> {
            Thread.dumpStack();
                            return true;
                        }, n -> {
                            Thread.dumpStack();
            return Long.valueOf(n.longValue());
                        }, TargetMappingPrecedence.LOWEST)
                        .build())
                .logHandler(System.out)
                .err(System.err)
                .build());

        try (Context context = contextLocal.get()) {
            MyObject myobj = new MyObject();

            Value bindings = context.getBindings("js");
            bindings.putMember("box", new MyBox());
            bindings.putMember("myobj", myobj);

            Value eval = context.eval("js", "myobj.setter1(1001)");
            assertEquals(Long.valueOf(1001), myobj.getter());

            eval = context.eval("js", "myobj.setter2(2001)");
            assertEquals(Integer.valueOf(2001), myobj.getter());

            eval = context.eval("js", "myobj.setter2(BigInt(2002))");
            assertEquals(Short.valueOf((short) 2002), myobj.getter());

            eval = context.eval("js", "myobj.setter3(3001)");
            assertEquals("3001", myobj.getter());

            eval = context.eval("js", "myobj.setter4(java.lang.Long.valueOf('4001'))");
            assertEquals(Long.valueOf(4001), myobj.getter());

            //eval = context.eval("js", "myobj.setter5(java.lang.Long.valueOf('5001'))");
            //eval = context.eval("js", "myobj.setter5(Java.to(5001, 'long'))");
            assertEquals(Long.valueOf(5001), myobj.getter());

        } catch (Exception x) {
            x.printStackTrace();
        } finally {
        }
    }

    public class MyObject {

        Object value;

        public Object getter() {
            return value;
        }

        public void setter1(String o) {
            System.out.println("set string " + o + " " + o.getClass());
            this.value = o;
        }

        public void setter1(Object o) {
            System.out.println("set object " + o + " " + o.getClass());

            this.value = o;
        }

        public void setter1(Long l) {
            System.out.println("set 1 long " + l + " " + l.getClass());
            this.value = l;
        }

        public void setter2(Object o) {
            System.out.println("set 2 object " + o + " " + o.getClass());

            this.value = o;
        }

        public void setter3(String o) {
            System.out.println("set 3 string " + o + " " + o.getClass());

            this.value = o;
        }

        public void setter4(long l) {
            System.out.println("set 4 long " + l);

            this.value = l;
        }

        public void setter5(Object l) {
            System.out.println("set 5 object " + l + " " + l.getClass());

            this.value = l;
        }
    }

    public class MyBox {

        public final Long toLong(Long l) {
            System.out.println("box long " + l + " " + l.getClass());
            return l;
        }
    }
}
