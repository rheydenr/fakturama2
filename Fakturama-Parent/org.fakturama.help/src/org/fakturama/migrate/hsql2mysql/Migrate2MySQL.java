/**
 * 
 */
package org.fakturama.migrate.hsql2mysql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author rheydenreich
 *
 */
public class Migrate2MySQL {

	/**
	 * @param args
	 */
    public static void main(String[] args) {
        try {
        	Properties migProps = new Properties();
        	InputStream propStream = Files.newInputStream(Paths.get(args[0]), StandardOpenOption.READ);
			migProps.load(propStream );
        	String files[] = new String[] {"migration.tpl", "migration_002.tpl", "migration_003.tpl"};
        	for (String file : files) {
	            Path path = Paths.get("c:\\Users\\rheydenreich\\Downloads\\HSQL2MYSQL4FAK\\", file);
	            Stream<String> lines = Files.lines(path);
	            
				Function<String, String> replaceFunction = migProps.entrySet().stream()
						.map(propEntry -> (Function<String, String>) line -> {
							String key = Pattern.quote("{{" + (String) propEntry.getKey() + "}}");
							String value = (String) propEntry.getValue();
							return line.replaceAll(key, value);})
						.reduce(Function.identity(), Function::andThen);
	            
	            List <String> replaced = lines.map(replaceFunction).collect(Collectors.toList());
	            Files.write(Paths.get(file.replaceAll("\\.tpl", ".grf")), replaced);
	            lines.close();
	            System.out.println("Find and Replace done!!!");
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
