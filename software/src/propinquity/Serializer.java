package propinquity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * This helper class provides methods to serialize and deserialize data to a file without much overhead.
 *
*/
public class Serializer {
	private Serializer() {}

	/**
	 * This method serializes an Object into a given file name for later retrieval.
	 *
	 * @param object the object to be serialized.
	 * @param path the filename and path to save the serialized object.
	 * @see #deserialize(Object object, String path) deserialize(T object, String path)
	*/
	public static <T> void serialize(T object, String path) {
	    try {
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(object);

            out.close();
            fileOut.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		System.out.println("->Serialization of "+path+" Done");
	}

	/**
	 * This method deserializes and Object from a given file. If the deserialized object is not of the generic type T, the method will return null.
	 *
	 * @param object an placeholder object to represent the type of the object to be deserialized.
	 * @param path the filename and path to load the serialized object from.
	 * @return the deserialized object. The return is null if the object was not of type T.
	 * @see #serialize(Object object, String path) serialize(T object, String path)
	*/
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(T object, String path) {
		T replacement_object = null;

		try {
	        FileInputStream fileIn = new FileInputStream(path);
	        ObjectInputStream in = new ObjectInputStream(fileIn);

	        replacement_object = (T)in.readObject();

	        in.close();
	        fileIn.close();
	    } catch (FileNotFoundException e) {
	    	return null;
	        // e.printStackTrace();
	    } catch (IOException e) {
	        return null;
	        // e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        return null;
	        // e.printStackTrace();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		System.out.println("->DeSerialization of "+path+" Done");

		return replacement_object;
	}

}