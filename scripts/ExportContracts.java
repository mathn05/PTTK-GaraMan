import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

/** Reflection export from compiled classes; never instantiates DAOs or reads configuration secrets. */
class ExportContracts {
    static String type(Type t){return t.getTypeName().replace('$','.');}
    static String params(Executable e){return Arrays.stream(e.getParameters()).map(p->type(p.getParameterizedType())+" "+p.getName()).collect(Collectors.joining(", "));}
    static String thrown(Executable e){return e.getExceptionTypes().length==0?"":" throws "+Arrays.stream(e.getExceptionTypes()).map(Class::getName).collect(Collectors.joining(", "));}
    public static void main(String[] args) throws Exception {
        Path root=Path.of("target/classes");List<String> names;
        try(var stream=Files.walk(root.resolve("garaman"))){names=stream.filter(p->p.toString().endsWith(".class")&&!p.toString().contains("$")).map(p->root.relativize(p).toString().replace('\\','.').replace('/','.').replaceAll("\\.class$","")).sorted().toList();}
        StringBuilder md=new StringBuilder("# Hợp đồng đầy đủ từ bytecode đã build\n\nSinh bằng scripts/ExportContracts.java; compiler bật -parameters, vì vậy tên/thứ tự tham số là tên source thật. Date là java.util.Date trừ LocalDate được ghi rõ. Runtime exceptions: mọi DAO SQL ném DataAccessException6; createAppointment ném BookingConflictException6 khi mất hold; validation ném IllegalArgumentException. Không tìm thấy object trả null, list trả mảng rỗng.\n\n");
        StringBuilder uml=new StringBuilder("@startuml\nhide empty members\nskinparam classAttributeIconSize 0\n");
        for(String name:names){
            Class<?> c=Class.forName(name,false,ExportContracts.class.getClassLoader());
            md.append("## ").append(name).append("\n\n```java\n").append(Modifier.toString(c.getModifiers())).append(" class ").append(c.getSimpleName());
            if(c.getSuperclass()!=null)md.append(" extends ").append(c.getSuperclass().getName());
            if(c.getInterfaces().length>0)md.append(" implements ").append(Arrays.stream(c.getInterfaces()).map(Class::getName).collect(Collectors.joining(", ")));
            md.append(" {\n");
            for(Field f:c.getDeclaredFields())if(!f.isSynthetic())md.append("  ").append(Modifier.toString(f.getModifiers())).append(" ").append(type(f.getGenericType())).append(" ").append(f.getName()).append(";\n");
            for(Constructor<?> ct:c.getDeclaredConstructors())if(!ct.isSynthetic())md.append("  ").append(Modifier.toString(ct.getModifiers())).append(" ").append(c.getSimpleName()).append("(").append(params(ct)).append(")").append(thrown(ct)).append(";\n");
            for(Method m:Arrays.stream(c.getDeclaredMethods()).sorted(Comparator.comparing(Method::getName)).toList())if(!m.isSynthetic())md.append("  ").append(Modifier.toString(m.getModifiers())).append(" ").append(type(m.getGenericReturnType())).append(" ").append(m.getName()).append("(").append(params(m)).append(")").append(thrown(m)).append(";\n");
            md.append("}\n```\n\n");
            boolean entity=name.startsWith("garaman.entity.");
            if(entity||name.startsWith("garaman.dao.")||name.startsWith("garaman.servlet.")){
                uml.append("class ").append(c.getSimpleName()).append(" {\n");
                for(Field f:c.getDeclaredFields())if(!f.isSynthetic()&&!Modifier.isStatic(f.getModifiers()))uml.append(Modifier.isPrivate(f.getModifiers())?"-":"#").append(f.getName()).append(" : ").append(f.getType().getSimpleName()).append("\n");
                if(!entity)for(Method m:c.getDeclaredMethods())if(Modifier.isPublic(m.getModifiers())&&!m.isSynthetic())uml.append("+").append(m.getName()).append("(").append(params(m)).append(") : ").append(m.getReturnType().getSimpleName()).append("\n");
                uml.append("}\n");
                if(c.getSuperclass()!=null&&c.getSuperclass().getName().startsWith("garaman."))uml.append(c.getSuperclass().getSimpleName()).append(" <|-- ").append(c.getSimpleName()).append("\n");
                if(entity)for(Field f:c.getDeclaredFields()){
                    Class<?> target=f.getType().isArray()?f.getType().getComponentType():f.getType();
                    if(target.getName().startsWith("garaman.entity."))uml.append(c.getSimpleName()).append(" --> ").append(target.getSimpleName()).append(" : ").append(f.getName()).append("\n");
                }
            }
        }
        Path destination=Path.of(args.length==0?"docs/final":args[0]);
        uml.append("@enduml\n");Files.createDirectories(destination.resolve("design"));
        Files.writeString(destination.resolve("01_Hop_dong_lop.md"),md,StandardCharsets.UTF_8);Files.writeString(destination.resolve("design/inventory-generated.puml"),uml,StandardCharsets.UTF_8);
        System.out.println("Exported exact field/method/constructor/parameter contracts for "+names.size()+" compiled classes.");
    }
}
