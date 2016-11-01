import java.util.Comparator;



public class HashFreqPair implements Comparator<HashFreqPair>{
		private int frequency;
		private String hashtag;
		HashFreqPair(String hashtag, int frequency){
			this.hashtag = hashtag;
			this.frequency = frequency;
		}
		
		public void incrementFrequency(){
			this.frequency = frequency+1;
		}
		
		public int getFrequency(){
			return this.frequency;
		}
		
		public String getHashTag(){
			return this.hashtag;
		}
		public boolean equals(Object h){
			if(h.getClass() != this.getClass())
				return false;
			if(this.hashtag.equals(((HashFreqPair)h).getHashTag()))
				return true;
			else
				return false;
		}
		
		public int compare(HashFreqPair x, HashFreqPair y) {
	    	 return x.getFrequency() - y.getFrequency();
	     }
		
		public int compareTo(HashFreqPair x){
			return (this.getFrequency() - x.getFrequency());
		}
		
	}
