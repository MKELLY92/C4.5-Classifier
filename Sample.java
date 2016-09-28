import java.util.ArrayList;

public class Sample{
	
	private ArrayList<String> values;
	private String classValue;
	
	public Sample(ArrayList values, String classValue) {
		this.values = values;
		this.classValue = classValue;
	}
	
	public Sample(Sample s) {
		this.values = s.values;
		this.classValue = s.classValue;
	}
	
	public String getClassValue(){
		return classValue;
	}

	public void setClassValue(String cV) {
		this.classValue = cV;
	}
	
	public String getAttribueValue(int attrID) {
		return (String) values.get(attrID);
	}
	
	public void setValues(ArrayList vals) {
		this.values = vals;
	}
	
	public int getNumberOfAttributes() {
		return values.size();
	}

}
