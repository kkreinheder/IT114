import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Class4 
{
	public static void main(String[] args)
	{
		List<String> myString = new ArrayList<String>();
		ArrayList<Integer> myInt = new ArrayList<Integer>();
		String[] stringArray = new String[3];
		
		myString.add("Hi");
		myString.add("Bye");
		myString.add("Hi again");
		myString.add("And I'm still here");
		myString.add("See you tomorrow");
		Collections.sort(myString);
		Collections.reverse(myString);
		Collections.shuffle(myString);
		
		stringArray[0] = "Hi";
		stringArray[1] = "Bye";
		stringArray[2] = "Hi again";
		
		String temp = stringArray[2];
		stringArray[2] = stringArray[0];
		stringArray[0] = temp; 
		
	//	myString.add(1);
	//	int myInt = 1;
		String testString =myInt +"";
	//	System.out.println(testString);
		if(testString.equals(myInt))
		{
	//		System.out.println("testString = myInt");
		}
		else
		{
	//		System.out.println("testString != myInt");
		}
		
	//	myString.set(2, "It's me");
		int size = myString.size();
		for(int i=0; i<size  ; i++)
		{
			if(i==1)
			{
			//	myString.remove(i);
			}
			System.out.println("Index[" + i +"] =>" + myString.get(i));
		//	System.out.println("Index[" + i +"] =>" + stringArray[i]);
		}
		
		for(int i=0; i<=10; i++)
		{
			myInt.add(i);
		}
		
		int sum=0;
		for(int i=0; i<myInt.size(); i++)
		{
			int t = myInt.get(i);
		//	sum = sum + t;
			sum += t;
		}
		System.out.println(sum);
		
		for(int i=0; i<myInt.size(); i++)
		{
			System.out.println("Index[" + i +"] =>" + myInt.get(i));
			
			if(myInt.get(i)%2 != 0)
			{
				System.out.println("Odd");
			}
			else
			{
				System.out.println("Even");
			}
		}
		
//		int size2 = myString.size();
//		for(int x=0; x<size2; x++);
//		{
//			System.out.println(shuffle(myString.get(x)));
//		}
	}
	
/*	public static void shuffle (List<String> list) //Knuth's shuffle
	{
		Random r = new Random();
	    int n = list.size();
	    while (n > 1) 
	    {
	        int k = r.nextInt(n--); //decrements after using the value
	        String temp = list.get(n);
	        String n1 = list.get(k);
	         n1 = temp;
	    }
	} */
}
