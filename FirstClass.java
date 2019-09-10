
public class FirstClass 
{
	public int test;
	private int test2;
	protected int test3;
	
	public String myString;
	public char myChar;
	
	public static void main(String[] args) 
	{
		FirstClass firstClass = new FirstClass();
		firstClass.test = 2;
		System.out.println(firstClass.test);

		for(int y = 0; y < 20; y++)
		{
			int count = 0;
			float floatCount = 0.0f;
			
			for(int i = 0; i < 10; i++)
			{
				count++;
				floatCount += 0.1f;
			}
			System.out.println("Count " + count);
			System.out.println("Float Count " + floatCount);
			
			if(floatCount == 1)
			{
				System.out.println("Wrong");
			}
		}
		
		String name = null;
		
		if("Bob".equals(name)) 
		{
			System.out.println("Bob equals name");
		}
		
		if(name.equals("Bob")) 
		{
			System.out.println("Bob equals name");
		}
		
		if("Bob" == name) 
		{
			System.out.println("Bob equals name");
		}
	}
}
