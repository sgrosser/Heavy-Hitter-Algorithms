import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Comparator;
public class HeavyHitter{
	//TODO:
	/*
	 * 
	 * Implement count-min sketch
	 * 
	 */
	
	
	public static final int k = 500; //For epsilon = 1/k in the count-min sketch
	public static final double CONFIDENCE = 0.999;
	public static final int numHashFunctions = (int) Math.log(1/(1-CONFIDENCE)) + 1;
	public static final int cmWidth = (int) (2.71*k);
	public static final int P = 1409; //First prime larger than cmWidth. Used for hashing. 
	
	public static int[][] hashFunctions = new int[numHashFunctions][2]; //represents a and b values
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		
		ArrayList<String> m = majority();
		System.out.println("--------------------------------------");
		ArrayList<String> cm = countMin();
		
		int countDifference = 0;
		for(String s : m){
			if(!cm.contains(s))
				countDifference++;
		}
		
		System.out.println(countDifference + ", " + m.size() +", " + cm.size());
	
	}
	
	public static ArrayList<String> majority() throws IOException{
		String str = "";
		FileReader fr = new FileReader("tweetstream.txt");
		BufferedReader br = new BufferedReader(fr);
		
		ArrayList<String> majHashTags = new ArrayList<String>();
		ArrayList<Integer> hashCntr = new ArrayList<Integer>();
		String ht;
		int m = 0;
		while((str = br.readLine()) != null){
			ht = parseHashTags(str).toLowerCase();
			if(ht.equals(""))
				continue;
			int index = majHashTags.indexOf(ht);
			if(index >= 0){
				hashCntr.set(index, hashCntr.get(index)+1);
			}
			else if(majHashTags.size()<k){ 
				majHashTags.add(ht);
				hashCntr.add(1);
			}
			else{
				for(int i = 0; i<hashCntr.size(); i++){
					hashCntr.set(i, hashCntr.get(i)-1);
					if(hashCntr.get(i) == 0){
						hashCntr.remove(i);
						majHashTags.remove(i);
						i--;
					}
				}
			}
			
		}
		
		for(int i = 0; i<majHashTags.size(); i++){
			System.out.println(majHashTags.get(i) + ": " + hashCntr.get(i));
		}
		return majHashTags;
	}
	
	public static ArrayList<String> countMin() throws IOException{
		
		String str = "";
		FileReader fr = new FileReader("tweetstream.txt");
		BufferedReader br = new BufferedReader(fr);
		
		//This is our min heap to keep track of elements
		PriorityQueue<HashFreqPair> freqPairs = new PriorityQueue<HashFreqPair>(1000, new Comparator<HashFreqPair>() {  
		    
            public int compare(HashFreqPair w1, HashFreqPair w2) {                         
                return w1.compareTo(w2);  
            }      
        });  
		     
		     
		
		//Our table set to zero
		int[][] cmSketch = new int[numHashFunctions][cmWidth];
		for(int i = 0; i<numHashFunctions; i++)
			for(int j = 0; j<cmWidth; j++){
				cmSketch[i][j] = 0;
			}
		
		generateHashFunctions();
		String ht; 
		int minFreq;
		int index;
		int counter = 0;
		//Update frequencies through the stream
		while((str = br.readLine()) != null){
			ht = parseHashTags(str).toLowerCase();
			if(ht.equals(""))
				continue;
			minFreq = 0;
			counter++;
			
			//update the frequency of the hashtag
			for(int i = 0; i<numHashFunctions; i++){
				index = hash(ht, i);
				cmSketch[i][index] += 1;
				if(i == 0)
					minFreq = cmSketch[i][index];
				else
					minFreq = (minFreq < cmSketch[i][index] ? minFreq : cmSketch[i][index]);
			}
			
			//Update the min heap
			if(minFreq > (double) counter/k ){
				HashFreqPair x = new HashFreqPair(ht, minFreq);
				if(freqPairs.contains(x)){
					freqPairs.remove(x);
					
				}
				
				freqPairs.offer(x);
			}
			
			//Remove from the min heap
			while(freqPairs.size()>0){
				HashFreqPair h = freqPairs.peek();
				
				//Minimum is a k-Majority element
				if(h.getFrequency() > counter/k)
					break;
				
				freqPairs.poll();
				
			}
			
			
		}
		
		ArrayList<String> hashtags = new ArrayList<String>();
		
		while(freqPairs.size() > 0){
			minFreq = 0;
			HashFreqPair h = freqPairs.poll();
			String s = h.getHashTag();
			
			//Find the actual frequency in the Count-Min Sketch
			for(int i = 0; i<numHashFunctions; i++){
				index = hash(s, i);
				
				if(i == 0)
					minFreq = cmSketch[i][index];
				else
					minFreq = (minFreq < cmSketch[i][index] ? minFreq : cmSketch[i][index]);
			}
			//System.out.println(minFreq + ", " + s);
			if(minFreq >= counter/k){
				System.out.println(h.getHashTag() + ": " + h.getFrequency());
				hashtags.add(s);
			}
			
			
		}
		
		
		return hashtags;
	}

	//Hash a string to an integer
	public static int hash(String s, int i){
		int code = s.hashCode();
		if(code < 0) code *= -1;

		BigInteger x = BigInteger.valueOf(hashFunctions[i][1]);
		BigInteger y = BigInteger.valueOf(code);
		x = x.multiply(y).add(BigInteger.valueOf(hashFunctions[i][0])).mod(BigInteger.valueOf(P));
		int mult = x.intValue();
		int index = mult % cmWidth;


		return index;
	}
	
	//Generate hash functions of the form ((a + bx) mod p) mod m
	public static void generateHashFunctions(){
		ArrayList<Integer> hashVals = new ArrayList<Integer>();
		
		Random rand = new Random();
		for(int i = 0; i< numHashFunctions; i++){
			hashFunctions[i][0] = rand.nextInt(P);
			if(!hashVals.contains(hashFunctions[i][0]))
				hashVals.add(hashFunctions[i][0]);
			else{
				i--;
			}
		}
		hashVals.clear();
		for(int i = 0; i< numHashFunctions; i++){
			hashFunctions[i][1] = rand.nextInt(P)+1;
			if(!hashVals.contains(hashFunctions[i][1]))
				hashVals.add(hashFunctions[i][1]);
			else{
				i--;
			}
		}
	}
	
	//For this problem, I am taking the first hashtag only from a tweet.
	public static String parseHashTags(String tweet){
		int initialIndex = tweet.indexOf(":{\"hashtags\":");
		if(initialIndex == -1)
			return "";
		
		String hashtags = tweet.substring(initialIndex);
		hashtags = hashtags.substring(hashtags.indexOf("["));
		hashtags = hashtags.substring(hashtags.indexOf(":")+2);
		hashtags = hashtags.substring(0,hashtags.indexOf("\""));
		
		if(hashtags.indexOf("\\u") >= 0)
			return "";
		return (hashtags.equals("],") || hashtags.equals("{") ? "" : hashtags);
		
	}

}
