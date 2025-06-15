/* 
 * 12/14/22
 * Names: Jesus Rodriguez and Zain Khatri
 *
 * This program compresses a source file into a target file using the
 * Huffman coding method. First, using ObhectOutputStream to output the
 * Huffman codes into the target file, then using BitOutputStream to output
 * the encoded binary contents to the target file. Pass the files from the
 * command line.
 *
 */
import java.io.*;
import java.util.*;

public class Compress_a_File {

    public static void main (String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {

		//String pathName = "/Users/juanrodriguez/Desktop/sourceFile.txt";
		//File sourceFile = new File(pathName);
		//FileInputStream fis = new FileInputStream(sourceFile);


        FileInputStream fis = new FileInputStream( new File(args[0]));


        String nameOfTargetFile = args[1];

        //Now creating a byte array of same length of the sourceFile's bytes

        byte[] bytes = new byte[(int)fis.available()];
        fis.read(bytes);
        String text = new String(bytes);

        System.out.print("The file reads:");
        System.out.println();
        System.out.println(text);

        spacing();

        int[] counts = getCharacterFrequency(text); //getting the frequency of each character
        System.out.printf("%-15s%-15s%-15s%-15s\n","ASCII Code", "Character", "Frequency","Code");

        Tree huffmanTree = getHuffmanTree(counts); // with counts[], getting the huffmanTree
        String[] codes = getCode(huffmanTree.root);

        for(int i =0; i < codes.length;i++) {
            if(counts[i] != 0) { //(char)i is not in text if counts[i] is 0
                System.out.printf("%-15s%-15s%-15s%-15s\n", i, (char)i+"",counts[i],codes[i]);
            }
        }

        spacing();


        //using codes[], this section loops through the file's text and appends the huffman
        //code belonging to each character to a StringBuilder called huffmanVersion
        StringBuilder huffmanVersion = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            huffmanVersion.append(codes[text.charAt(i)]);
        }

        System.out.println("Showing the huffman version of the source file:");
        System.out.print(huffmanVersion);

        spacing();

        //Opening the ObjectOutputStream to write the huffman codes and tree onto
        //the targetFile
        //String nameOfTargetFile = "testing12.txt";
        File targetFile = new File(nameOfTargetFile);
        FileOutputStream fos = new FileOutputStream(targetFile);

        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(codes);
        oos.writeObject(huffmanTree);


        //writing the huffman version of the file using bit output stream first
        System.out.println("Now writing the huffmanVersion onto " + nameOfTargetFile + " using BitOutputStream...");

        BitOutputStream bos = new BitOutputStream(fos);
        String data = huffmanVersion.toString();
        bos.writeBit(data);


        bos.close();
        fos.close();
        oos.close();

        System.out.println("Done!");

    }


    public static void spacing() {
        System.out.println();
        System.out.println();

    }
    public static String[] getCode(Node root) {
        if(root == null)return null;
        String[] codes = new String[2*128];
        assignCode(root,codes);
        return codes;
    }
    private static void assignCode(Node root, String[] codes) {
        if(root.left !=null) {
            root.left.code = root.code+ "0";
            assignCode(root.left,codes);//Recursive call

            root.right.code=root.code+"1";
            assignCode(root.right,codes);//Recursive call
        }else {
            codes[(int)root.element] = root.code;
        }
    }



    public static Tree getHuffmanTree(int[] counts) {
        Heap<Tree> heap = new Heap<>();//Create a new heap to hold trees
        for(int i=0; i < counts.length;i++) {
            if(counts[i] > 0) {
                heap.add(new Tree(counts[i],(char)i));//A leaf node tree
            }
        }
        while(heap.getSize() > 1) {
            Tree t1 = heap.remove();//Remove the smalled weightTree
            Tree t2 = heap.remove();//Remove the next smallest
            heap.add(new Tree(t1,t2));//Combines the two trees
        }
        return heap.remove();
    }
    public static int[] getCharacterFrequency(String text) {//Gets the frequency of characters
        int[] counts = new int[256];//256 ASCII characters
        for(int i = 0; i < text.length(); i++) {
            counts[(int)text.charAt(i)]++;//Count the characters in text
        }
        return counts;
    }
}

class Tree implements Comparable<Tree>, Serializable  {
    public Node root; // root of the tree

    //Given 2 subtrees, the constructor combines them to make a
    //bigger Tree --> will be used in getHuffmanTree(int[] counts)
    //See Fig: 25.19 (b) through (d)
    public Tree(Tree t1, Tree t2) {
        root = new Node();
        root.left = t1.root;
        root.right = t2.root;
        root.weight = t1.root.weight + t2.root.weight;
    }

    //given weight and element, a Tree is made from a single Node
    //This constructor will be used to make the individual trees
    //See Fig: 25.19 (a)
    public Tree(int weight, char element) {
        root = new Node(weight, element);
    }

    @Override //Compares Nodes based on weight
    public int compareTo(Tree t) {
        if (root.weight < t.root.weight) // Purposely reverse the order
            return 1;
        else if (root.weight == t.root.weight)
            return 0;
        else
            return -1;
    }

}

class Node implements Serializable {
    char element; // Stores the character for a leaf node
    int weight; // weight of the subtree rooted at this node
    Node left; // Reference to the left subtree
    Node right; // Reference to the right subtree
    String code = ""; // The code of this node from the root

    public Node() {
    }

    public Node(int weight, char element) {
        this.weight = weight;
        this.element = element;
    }
}



class Heap<E extends Comparable<E>> {
    private java.util.ArrayList<E> list = new java.util.ArrayList<E>();

    // Creating a default heap
    public Heap() {
    }

    // Creating a heap from an array of objects
    public Heap(E[] objects) {
        for (int i = 0; i < objects.length; i++)
            add(objects[i]);
    }

    // Adding a new object into the heap
    public void add(E newObject) {
        list.add(newObject); // Append to the heap
        int currentIndex = list.size() - 1; // The index of the last node

        while (currentIndex > 0) {
            int parentIndex = (currentIndex - 1) / 2;
            // Switch the current object if its greater than parent
            if (list.get(currentIndex).compareTo(list.get(parentIndex)) > 0) {
                E temp = list.get(currentIndex);
                list.set(currentIndex, list.get(parentIndex));
                list.set(parentIndex, temp);
            } else {
                break;
            }

            currentIndex = parentIndex;
        }
    }

    // Removing the root from the heap
    public E remove() {
        if (list.size() == 0)
            return null;

        E removedObject = list.get(0);
        list.set(0, list.get(list.size() - 1));
        list.remove(list.size() - 1);

        int currentIndex = 0;
        while (currentIndex < list.size()) {
            int leftChildIndex = 2 * currentIndex + 1;
            int rightChildIndex = 2 * currentIndex + 2;

            // Find the maximum between two children
            if (leftChildIndex >= list.size()) {
                break; // The tree is a heap
            }

            int maxIndex = leftChildIndex;
            if (rightChildIndex < list.size()) {
                if (list.get(maxIndex).compareTo(list.get(rightChildIndex)) < 0) {
                    maxIndex = rightChildIndex;
                }
            }

            // Swap if the current node is less than the maximum
            if (list.get(currentIndex).compareTo(list.get(maxIndex)) < 0) {
                E temp = list.get(maxIndex);
                list.set(maxIndex, list.get(currentIndex));
                list.set(currentIndex, temp);
                currentIndex = maxIndex;
            } else
                break; // The tree is a heap
        }

        return removedObject;
    }

    // Geting the number of nodes in the tree
    public int getSize() {
        return list.size();
    }
}

class BitOutputStream {
    private FileOutputStream output;

    byte bitBuffer = 0;
    int bitCounter = 0;

    //Constuctor
    public BitOutputStream(File file) throws IOException {
        output = new FileOutputStream(file);
    }

    public BitOutputStream(FileOutputStream fos) throws IOException {
        output = fos;
    }

    //This method adds a character to the bitBuffer if
    //the bitCounter reaches 8, the bitBuffer's content
    //is added to the file.
    public void writeBit(char bit) throws IOException {
        bitBuffer = (byte) (bitBuffer << 1); //adds a 0 to the bytebuffer by shifting to left

        if (bit == '1') {
            bitBuffer = (byte) (bitBuffer | 1); // this changes the 0 previously added to 1
        }
        bitCounter++;

        //If bitCounter reaches 8, the bitBuffer's contents is writen to the file
        //and bitCounter and bitBuffer is resetted.
        if (bitCounter == 8) {
            output.write(bitBuffer);

            bitBuffer = 0;
            bitCounter = 0;
        }
    }

    //This method cycles through the bitString and uses writeBit(char bit) to
    //add bits to the bitBuffer
    public void writeBit(String bitString) throws IOException {
        for (int i = 0; i < bitString.length(); i++) {
            writeBit(bitString.charAt(i));
        }
    }

    //This method fills the bitBuffer with 0's so the remaining bits
    //can be uploaded to the file.
    public void close() throws IOException {
        if (bitCounter > 0) { //checks to see if there are still bits remaining
            bitBuffer = (byte) (bitBuffer << (8 - bitCounter) );
            output.write(bitBuffer);
        }
        output.close();// This makes use of the close() method for a FileOutputStream object
    }
}












