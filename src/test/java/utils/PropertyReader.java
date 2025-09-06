package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {
	
	public static Properties readProperties() {
		
		String fileName="src\\test\\resources\\config\\config.properties";
		Properties prop=null;
		
		try {
			FileInputStream fis=new FileInputStream(fileName);
			prop=new Properties();
			prop.load(fis);		
		} 
		catch (FileNotFoundException e) {
			System.out.println("File name is not correct, please check the file name");
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop;
	}
}
