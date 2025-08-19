package rs.jamie.luneth.annotations;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes("rs.jamie.luneth.annotations.LunethSerializer")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class FieldProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement compareElement = processingEnv.getElementUtils()
                .getTypeElement(LunethField.class.getCanonicalName());

        for (Element classElement : roundEnv.getElementsAnnotatedWith(LunethSerializer.class)) {
            if (classElement.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@LunethSerializer only accepts classes");
                continue;
            }

            boolean hasKey = false;
            List<Integer> order = new ArrayList<>();

            for (Element element : classElement.getEnclosedElements()) {
                if (element.getKind() != ElementKind.FIELD) continue;

                for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
                    if(!processingEnv.getTypeUtils().isSameType(mirror.getAnnotationType(), compareElement.asType())) continue;
                    Map<String, Object> values = new HashMap<>();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                            mirror.getElementValues().entrySet()) {

                        String key = entry.getKey().getSimpleName().toString();
                        Object value = entry.getValue().getValue();
                        values.put(key, value);
                    }

                    boolean key = (boolean) values.getOrDefault("key", false);
                    int id = (int) values.getOrDefault("id", 0);

                    // Validate annotation key
                    if (key) {
                        if (hasKey) {
                            messager.printMessage(Diagnostic.Kind.ERROR, "@LunethField: Multiple keys found", element);
                        }
                        if (id != 0) {
                            messager.printMessage(Diagnostic.Kind.ERROR, "@LunethField: Keys must not define an ID (or use 0)", element);
                        }
                        hasKey = true;
                    } else if (id == 0) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "@LunethField: Must define an ID >0 for non-key fields", element);
                    }

                    // Validate annotation id
                    if (order.contains(id)) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "@LunethField: Duplicate ids found", element);
                    } else {
                        order.add(id);
                    }
                }
            }

            if(!hasKey) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@LunethField: Must define key field");
            }
            if(order.size() < 2) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@LunethField: Must define at least 2 fields");
            }
        }
        return false;
    }
}
