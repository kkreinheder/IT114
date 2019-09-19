import java.util.LinkedList; 
import java.util.Queue; 
  
public class QueueExample 
{ 
  public static void main(String[] args) 
  { 
    Queue<Object> q = new LinkedList<>(); 
    
    add(q);
    remove(q);
    size(q);  
    peek(q); 
    System.out.println(q);
  } 
  
  public static void add( Queue<Object> q)
  {
	  for (int i=0; i<10; i++) 
	  {
	  q.add(new CustomQueue(i)); 
	  }
	  System.out.println("Numbers in queue: " + q);  
  }
  public static void remove( Queue<Object> q)
  {
	  Object remove = q.remove(); 
	  System.out.println("Removed number: " + remove);  
  }
  public static void size( Queue<Object> q)
  {
	  int size = q.size(); 
	  System.out.println("The current size of the queue is: " + size);
  }
  public static void peek( Queue<Object> q)
  {
	  Object head = q.peek(); 
	  System.out.println("The current head of the queue is: " + head);
  }
  
  static class CustomQueue
  {
	  public int value;
	  public CustomQueue(int q)
	  {
		value = q;
	  }
	@Override
	public String toString() 
  	{
	  return "Value: " + value;
	}
  }
} 