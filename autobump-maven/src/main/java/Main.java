import com.github.autobump.core.model.Workspace;
import com.github.autobump.maven.model.MavenDependencyResolver;

public class Main {
    public static void main(String[] args) {
        MavenDependencyResolver resolver = new MavenDependencyResolver();
        resolver.resolve(new Workspace("D:\\school\\2019-2020\\stage\\autobump\\autobump\\autobump-maven\\src\\test\\resources\\multi_module_root"));

    }
}
