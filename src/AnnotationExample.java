import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhangxinwei on 7/26/16.
 */

@Test("col")
public class AnnotationExample extends Thread{

    public static void main(String[] args) {
        if (AnnotationExample.class.isAnnotationPresent(Test.class)) {
            Test t = (Test)AnnotationExample.class.getAnnotation(Test.class);
            System.out.println(t.value());
        }

    }

    @Override
    public void run() {
        super.run();
    }
}


@Retention(RetentionPolicy.RUNTIME)
@interface Test {
    String value() default "";
}