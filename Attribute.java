import java.util.ArrayList;

public class Attribute {

	private static int attributeID;
	private static ArrayList values;
	
	public Attribute(int ID) {
		attributeID = ID;
		
	}
	
	public ArrayList getValues() {
		return this.values;
	}
	
	public void addValue(Object value) {
		values.add(value);
	}
}
