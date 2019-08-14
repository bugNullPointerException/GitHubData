public class MyTest {
    public static void main(String[] args) {
        String str = "hello world java scala";
        String[] arr = str.split(" ", -1);
        for (String s : arr) {
            System.out.println(s);
        }
    }
}
