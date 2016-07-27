/**
 * Created by zhangxinwei on 7/25/2016.
 */
public class A_main {

    public static void main(String[] args) {
        System.out.println("ss");

        Parse.pagedirPath = "/Users/zhangxinwei/all_my_project_dir_/demo/htmlpage";
        Parse.start();

        HtmlTemplate.compile();
    }
}
