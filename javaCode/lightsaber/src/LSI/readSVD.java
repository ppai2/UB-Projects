package LSI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class readSVD {
	
	static int DIMENTIONS = 20;
	static int baseDocsLength = 10434;   
	static int docLength;
	static String collectionPath="";     // ENTER COLLECTION PATH!!
	static String collection2 = collectionPath+File.separator+"indexes"+File.separator+"tor";

	
	public static void main(String[] args) throws IOException {
		int[][] relatedDocs = new int[baseDocsLength][3];
		HashMap<Integer, ArrayList<Integer>> relatedDocsMap = new HashMap<Integer, ArrayList<Integer>>();
		for(int i=0;i<baseDocsLength;i++){
			ArrayList<Integer> relatedDocIDList;
			relatedDocIDList = findTopRelatedDocs(i);   
			if(null!=relatedDocIDList && relatedDocIDList.size()>0){
				relatedDocsMap.put(i, relatedDocIDList);
				for(int j=0;j<relatedDocIDList.size();j++)
					relatedDocs[i][j] = relatedDocIDList.get(j);
			}

		}
		saveMapToFile(relatedDocsMap);
 
     

	}

	private static void saveMapToFile(
			HashMap<Integer, ArrayList<Integer>> relatedDocsMap) {
		try {
			File file = new File(collectionPath+File.separator+"RelatedDocs"+File.separator+"relatedTorMap");
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(relatedDocsMap);
			oos.close();
			fos.close();
		} catch (FileNotFoundException f) {
			f.getMessage();
		} catch (IOException e) {
			e.getMessage();
		} 
		
		
	}

	private static ArrayList<Integer> findTopRelatedDocs(int baseDocID) throws NumberFormatException, IOException {
		BufferedReader in;
		
		try{
		in = new BufferedReader(new FileReader(collectionPath+File.separator+"SVD Matrix"+File.separator+"tor"+baseDocID+".txt"));
		} catch (FileNotFoundException e){
			System.out.println("FILE NOT FOUND. "+baseDocID);
			ArrayList<Integer> temp = null;
			return temp;
		}
		String str;
        docLength = docCountOfCollection2();
        //System.out.println("TECH doc LEngth:"+docLength);
        double[][] docVectors = new double[docLength+1][DIMENTIONS];
        int i=0;
        while ((str = in.readLine()) != null && i<docLength+1) {
       	 String[] docCords=str.split(",");
       	 for(int j = 0; j< docCords.length ; j++)
       	 {	 docVectors[i][j] = Double.parseDouble(docCords[j]);
       	 }
       	 if(docCords.length<DIMENTIONS){
       		 for(int j = DIMENTIONS-1; j>= docCords.length ; j--)
       			docVectors[i][j] = 0.0;
       	 }
       	 i++;
        }
        in.close();
        
        
		//int[] docIDList = new int[3];
		ArrayList<Integer> docIDList = new ArrayList<Integer>();
        
        Map<Integer, Double> docDistanceMap = new HashMap<Integer, Double>();
		for (i = 1; i < docVectors.length; i++) {
			double dist = distance(docVectors[0], docVectors[i]);
			//System.out.println("--------  " + dist);
			docDistanceMap.put(i - 1, dist); // Actual value of Docs is
												// returned. Hence 'i-1'.
		}

		//System.out.println("\n\nSORTED:");

		ValueComparator bvc = new ValueComparator(docDistanceMap);
		TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
		sorted_map.putAll(docDistanceMap);

		Iterator it = sorted_map.entrySet().iterator();
		int j=0, maxDocs = 3, zerodocs=0; i=0;
		boolean hasPositive=false;
		double xpoint = 0, ypoint = 0;
		double[] points = new double[19];
		while (it.hasNext() && i<maxDocs) {
			Map.Entry pairs = (Map.Entry) it.next();
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);            
			nf.setGroupingUsed(false);
			
			for(int k=0;k<19;k++){
				points[k] = roundTwoDecimals(docVectors[(int)pairs.getKey()+1][k+1]);
				if(points[k]!=0 && points[k]!=-0)
					hasPositive = true;
			}
			if(hasPositive==false)
				continue;
			/*xpoint = roundTwoDecimals(docVectors[(int)pairs.getKey()+1][1]);
			ypoint = roundTwoDecimals(docVectors[(int)pairs.getKey()+1][2]);
			System.out.println(xpoint+"    "+docVectors[(int)pairs.getKey()+1][1]+ "    "+ ypoint+"   "+ docVectors[(int)pairs.getKey()+1][2]);*/
			j++;
			/*if((xpoint==0.00 || xpoint==-0.00) && (ypoint==0.00 || ypoint==-0.00))
				continue;*/
			//System.out.println("yes");
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());
			//docIDList[i] = (int) pairs.getKey();
			docIDList.add((int) pairs.getKey());
			i++;
		}
		return docIDList;
	}
	
	static double roundTwoDecimals(double d) { 
	      DecimalFormat twoDForm = new DecimalFormat("#.###"); 
	      return Double.valueOf(twoDForm.format(d));
	} 

	private static double distance(double[] ds, double[] ds2) {
		double inRoot = 0;
		
		for(int i=1;i<DIMENTIONS;i++)
			inRoot += Math.pow((ds[i] - ds2[i]),2);
		return Math.sqrt(inRoot);
		
	}
	
	private static int docCountOfCollection2() throws IOException {
		Directory dirIndex = FSDirectory
				.open(new File(collection2));
		IndexReader ir = IndexReader.open(dirIndex);
		
		return ir.getDocCount("id");
	}

}


class ValueComparator implements Comparator<Integer> {

	Map<Integer, Double> base;

	public ValueComparator(Map<Integer, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(Integer a, Integer b) {
		if (base.get(a) < base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}