/*
 *  12/14/22
 * Names: Jesus Rodriguez and Zain Khatri
 *
 * This second program decompresses a previously compressed file so that
 * it replicates the original source file in Part 1. This is done in the
 * command line.
 *
 */

import java.io.*;
import java.util.*;
public class Decompress_a_File {

    public static void main (String[] args) throws IOException, ClassNotFoundException  {
        File sourceFile = new File(args[0]);
        String nameOfTargetFile = args[1];

        //Setting streams up
        FileInputStream fis = new FileInputStream(sourceFile);
        ObjectInputStream ois = new ObjectInputStream(fis);

        //Retreiveing codes and huffmanTree from compressed file
        String[] codes = (String[])ois.readObject();
        Tree huffmanTree = (Tree)ois.readObject();

        //the remaining bytes should be the huffman string, so retrieving
        //that as well and turning the bytes into a String of characters
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes);


        StringBuffer preHuffman = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            preHuffman.append(toBinaryString(bytes[i]));
        }

        //converting StringBuffer's content into a String
        String huffmanVersion = preHuffman.toString();

        //with huffmanTree and huffmanVersion,calling the decompressData()
        String decompressedData = decompressData(huffmanTree, huffmanVersion);

        //Writing the decompressed data onto the targetFile

        System.out.println("Content being written onto file \"" + nameOfTargetFile + "\":");
        System.out.println(decompressedData);

        BufferedWriter writer = new BufferedWriter(new FileWriter(nameOfTargetFile));
        writer.write(decompressedData);
        writer.close();

        System.out.println();
        System.out.println("Done!");
    }

    //this method reverts a byte back into the original character
    //this will be used to revert the bytes of the file back to the
    //huffman version string
    public static String toBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {

            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }

    //using a huffman tree and the huffman string, the method traveres through
    //the huffman tree to decompress the file
    public static String decompressData(Tree huffmanTree , String huffmanVersion) {
        StringBuilder data = new StringBuilder();
        if (huffmanTree.root == null) {
            return null;
        }

        Node pointer = huffmanTree.root;
        for (int i = 0; i < huffmanVersion.length(); i ++) {
            char digit = huffmanVersion.charAt(i);

            //traverses left or right depending on digit
            if (digit == '1') {
                pointer = pointer.right;
            } else {
                pointer = pointer.left;
            }
            //After traversing one node down, checks to see if the pointer
            //is pointing at leaf node. If it is, appending the node's element
            //to the string builder and makes pointer point back up to the root
            if (isLeaf(pointer)) {
                data.append(pointer.element);
                pointer = huffmanTree.root;
            }
        }

        data.deleteCharAt(data.length() - 1); // thish takes into account the extra 0's added to the file when
        //the bitOutputStream is closed
        return data.toString();

    }
    //helper method to check if node is leaf
    private static boolean isLeaf(Node node) {
        return (node.left == null && node.right == null);
    }
}

class Tree implements Comparable<Tree>, Serializable  {
    public Node root; // root of the tree


    public Tree(Tree t1, Tree t2) {
        root = new Node();
        root.left = t1.root;
        root.right = t2.root;
        root.weight = t1.root.weight + t2.root.weight;
    }

    public Tree(int weight, char element) {
        root = new Node(weight, element);
    }

    @Override
    public int compareTo(Tree t) {
        if (root.weight < t.root.weight)
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

