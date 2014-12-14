package LSI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.ByteRunAutomaton;
import org.apache.solr.client.solrj.beans.Field;

public class TermDocMatrix {
	static String collection = ""; // ENTER COLLECTION LOCATION
	static String collection1 = collection + File.separator + "indexes"
			+ File.separator + "pedia";
	static String collection2 = collection + File.separator + "indexes"
			+ File.separator + "tor";
	static String field1 = "body";// "name";
	static String field1_t = "title";// "name";
	static String field2 = "body";// "name";
	static String field2_t = "title";

	public static void main(String[] args) throws IOException {

		Directory dirIndex = FSDirectory.open(new File(collection1));
		IndexReader ir = IndexReader.open(dirIndex);

		int docID = 0;
		double[][] tm;
		int totalDocs = ir.getDocCount(field1);

		for (int i = 9540; i < totalDocs; i++) {
			String[] titleTerms = ir.document(i).get(field1_t).toString()
					.split(" ");
			docID = i;
			tm = getTermDocMatrixForDocument(docID, ir, titleTerms);
			// if(null!=tm)
			saveTminFile(tm, docID);
			tm = null;

		}
		/*
		 * displayArray(tm);/* if(tm.length>0)
		 * System.out.println("-----Length111  "+tm[0].length+"  "+tm.length);
		 */
		ir.close();
		dirIndex.close();

		tm = null;
	}

	private static double[][] getTermDocMatrixForDocument(int docID,
			IndexReader ir, String[] titleTerms) throws IOException {
		double[][] tm = null;

		Terms terms;
		terms = ir.getTermVector(docID, field1);

		tm = makeMatrix(terms, docID, titleTerms);

		return tm;
	}

	private static double[][] makeMatrix(Terms terms, int docid,
			String[] titleTerms) throws IOException {
		Directory dirIndex = FSDirectory.open(new File(collection2));
		IndexReader ir = IndexReader.open(dirIndex);
		int docCount2 = docCountOfCollection2();
		double[][] tm = null;
		if (terms != null && terms.size() > 0) {
			TermsEnum termsEnum = terms.iterator(null);
			int termcount = 0; // this field
			int titleTermsCount = titleTerms.length;
			try {
				tm = new double[(int) terms.size() + titleTermsCount][docCount2 + 1];
			} catch (OutOfMemoryError f) {
				saveError(docid);
				return null;
			}
			BytesRef term = null;
			while ((term = termsEnum.next()) != null) {

				Term tr = new Term(term.utf8ToString());
				int freq = 0;

				tm[termcount][0] = roundTwoDecimals(1 + Math.log10(termsEnum
						.totalTermFreq()));

				// System.out.println(term.utf8ToString() +"  "+
				// termsEnum.totalTermFreq());

				HashMap<Integer, Double> docFreqMap = new HashMap<Integer, Double>();

				if (MultiFields.getTermDocsEnum(ir,
						MultiFields.getLiveDocs(ir), field2, term) != null) {

					DocsEnum docEnum = MultiFields.getTermDocsEnum(ir,
							MultiFields.getLiveDocs(ir), field2, term);

					int doc;
					while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
						freq++;
						docFreqMap
								.put((int) docEnum.docID(),
										roundTwoDecimals(1 + Math.log10(docEnum
												.freq())));
					}
				}

				if (MultiFields.getTermDocsEnum(ir,
						MultiFields.getLiveDocs(ir), field2_t, term) != null) {
					DocsEnum docEnum = MultiFields.getTermDocsEnum(ir,
							MultiFields.getLiveDocs(ir), field2_t, term); // For
																			// Title
					int doc;
					while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
						if (docFreqMap.containsKey(docEnum.docID()))
							docFreqMap
									.put((int) docEnum.docID(),
											docFreqMap.get(docEnum.docID())
													+ roundTwoDecimals(Math
															.log10(2 * docEnum
																	.freq()))); // Title
																				// weight
																				// applied
						else {
							freq++;
							docFreqMap.put((int) docEnum.docID(),
									1 + roundTwoDecimals(Math.log10(2 * docEnum
											.freq())));
						}
					}
				}

				// System.out.println(term.utf8ToString()+
				// "   TerfFreq: "+freq);

				for (int i = 0; i < docCount2; i++) {
					if (docFreqMap.containsKey(i))
						tm[termcount][i + 1] = docFreqMap.get(i)
								* Math.log10(docCount2 / freq);
					else
						tm[termcount][i + 1] = 0;
				}
				termcount++;
			}

			// /////////////////////////////////////////////////TITLE
			// TERMS/////////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for (int k = 0; k < titleTermsCount; k++) {

				int freq = 0;

				tm[termcount][0] = 5;

				// System.out.println(term.utf8ToString() +"  "+
				// termsEnum.totalTermFreq());

				HashMap<Integer, Double> docFreqMap = new HashMap<Integer, Double>();
				Random rn = new Random();
				BytesRef tterm = new BytesRef(titleTerms[k]);
				if (MultiFields.getTermDocsEnum(ir,
						MultiFields.getLiveDocs(ir), field2, tterm) != null) {

					DocsEnum docEnum = MultiFields.getTermDocsEnum(ir,
							MultiFields.getLiveDocs(ir), field2, tterm);

					int doc;
					while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
						int tt = termcount / 10;
						for (int x = 0; x < termcount; x++) {
							int rand = rn.nextInt(termcount) + 0;
							tm[rand][docEnum.docID() + 1] = tm[rand][0];
						}
						freq++;
						docFreqMap
								.put((int) docEnum.docID(),
										roundTwoDecimals(1 + Math.log10(docEnum
												.freq())));
					}
				}

				if (MultiFields.getTermDocsEnum(ir,
						MultiFields.getLiveDocs(ir), field2_t, tterm) != null) {
					// System.out.println(titleTerms[k]);
					DocsEnum docEnum = MultiFields.getTermDocsEnum(ir,
							MultiFields.getLiveDocs(ir), field2_t, tterm); // For
																			// Title
					int doc;
					while ((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
						int tt = termcount / 3;
						for (int x = 0; x < termcount; x++) {
							int rand = rn.nextInt(termcount) + 0;
							tm[rand][docEnum.docID() + 1] = tm[rand][0];
						}
						// System.out.println(docEnum.docID()+"   "+termcount);
						if (docFreqMap.containsKey(docEnum.docID()))
							docFreqMap.put(
									(int) docEnum.docID(),
									docFreqMap.get(docEnum.docID())
											+ 5
											* roundTwoDecimals(Math
													.log10(docEnum.freq()))); // Title
																				// weight
																				// applied
						else {
							freq++;
							docFreqMap.put((int) docEnum.docID(),
									1 + 5 * roundTwoDecimals(Math.log10(docEnum
											.freq())));
						}
					}
				}

				// System.out.println(titleTerms[k]+ "   TerfFreq: "+freq);

				for (int i = 0; i < docCount2; i++) {
					if (docFreqMap.containsKey(i))
						tm[termcount][i + 1] = docFreqMap.get(i)
								* Math.log10(docCount2 / freq);
					else
						tm[termcount][i + 1] = 0;
				}
				termcount++;
			}

			/*
			 * System.out.println("4988::: "+tm[562][4989]);
			 * 
			 * System.out.println("4988::: "+tm[563][4989]);
			 */

			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		}

		return tm;
	}

	private static boolean saveTminFile(double[][] tm, int docid)
			throws IOException {
		FileWriter fl = new FileWriter(collection + File.separator
				+ "termDocMatrix" + File.separator + "tor" + File.separator
				+ docid + ".txt");
		BufferedWriter bf = new BufferedWriter(fl);
		StringBuffer sb = new StringBuffer();
		StringBuffer sbTemp = new StringBuffer();
		if (null == tm)
			return false;
		int nrows = tm.length;
		/*
		 * if(nrows>2000) nrows=2000;
		 */
		int ncols = tm[0].length;
		String row = "";
		boolean addNewLine = true;
		for (int i = 0; i < nrows; i++) {
			addNewLine = true;
			// System.out.println(i);
			for (int j = 0; j < ncols; j++) {
				// row = row+String.valueOf(tm[i][j])+"   ";
				try {
					sb.append(String.valueOf(tm[i][j]) + "   ");
				} catch (OutOfMemoryError e) {
					i--;
					try {
						bf.write(sbTemp.toString());
					} catch (OutOfMemoryError f) {
						saveError(docid);
						sb = null;
						sbTemp = null;
						bf.close();
						fl.close();
						return false;
					}
					sb = null;
					sbTemp = null;
					bf.close();
					fl.close();
					fl = new FileWriter(collection + File.separator
							+ "termDocMatrix" + File.separator + "tor"
							+ File.separator + docid + ".txt", true);
					bf = new BufferedWriter(fl);
					sb = new StringBuffer();
					sbTemp = new StringBuffer();
					addNewLine = false;
					break;
				}

				// System.out.print(tm[i][j]+"   ");
			}
			sbTemp = null;
			sbTemp = sb;
			if (addNewLine)
				sb.append("\n");

		}
		try {
			bf.write(sb.toString());
		} catch (OutOfMemoryError f) {
			saveError(docid);
			sb = null;
			sbTemp = null;
			bf.close();
			fl.close();
			return false;
		}
		tm = null;
		bf.close();
		fl.close();
		return true;
	}

	private static void saveError(int docid) throws IOException {
		FileWriter fl = new FileWriter(collection + File.separator
				+ "Errors.txt", true);
		BufferedWriter bf = new BufferedWriter(fl);
		StringBuffer sb = new StringBuffer();
		sb.append("\n" + docid);
		bf.write(sb.toString());
		bf.close();
		fl.close();
	}

	private static int docCountOfCollection2() throws IOException {
		Directory dirIndex = FSDirectory.open(new File(collection2));
		IndexReader ir = IndexReader.open(dirIndex);

		return ir.getDocCount("id");
	}

	private static void displayArray(double[][] tm) {
		System.out.print("[");
		for (int i = 0; i < tm.length; i++) {
			for (int j = 0; j < tm[0].length; j++) {
				if (j != 0)
					System.out.print(", ");
				System.out.print(tm[i][j]);

			}
			System.out.println(";");
		}
		System.out.print("]");

	}

	static double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.###");
		return Double.valueOf(twoDForm.format(d));
	}
}