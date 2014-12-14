package datasetExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.xml.sax.InputSource;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.CanolaExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

public class BoilerpipeParser {
	static String resourcePath = "C:"+File.separator+"xamp"+File.separator+"htdocs";
	static String torPath = resourcePath+File.separator+"tor"+File.separator+"blog";//              
	static String sfePath = resourcePath+File.separator+"sfe"+File.separator+"entry";//              
	static String techContentPath = resourcePath+File.separator+"technovelgy"+File.separator+"content";//              
	static String techNewsPath = resourcePath+File.separator+"technovelgy"+File.separator+"Science-Fiction-News";//              
	public static void main(String[] args)
			throws BoilerpipeProcessingException, IOException {

		TOR_Extract();
		SF_ENC_Extract();
		TECHNO_Content_Extract();
		TECHNO_SciFiNews_Extract();
	}

	private static void TECHNO_SciFiNews_Extract()
			throws MalformedURLException, IOException {
		int totalPosts = 0;
		String blogsPath = techNewsPath; // "C:\\xampp\\htdocs\\technovelgy\\Science-Fiction-News";
		File blogsDir = new File(blogsPath);
		String[] blogPosts = blogsDir.list();
		for (String post : blogPosts) {
			if (post.endsWith(".html")) {
				URL blogPostUrl = new URL(
						"http://localhost/technovelgy/Science-Fiction-News/"
								+ post);
				if (checkResponse(blogPostUrl) == 404)
					continue;

				String text = "";
				try {
					text = ArticleExtractor.INSTANCE.getText(blogPostUrl);
				} catch (BoilerpipeProcessingException e) {
					e.printStackTrace();
				}

				text = text.replaceAll("\n", "------newline------");
				text = text.replaceAll("\\s+", " ").trim();

				if (text.contains("Home | Glossary | Timeline | New"))
					text = text.replace("Home | Glossary | Timeline | New", "");

				text = text.replaceAll("------newline------", "\n").trim();

				if (!text.trim().isEmpty() && !text.trim().equals("")
						&& text.trim().length() > 2) {
					post = post.substring(0, post.lastIndexOf(".html"));
					File file = new File(blogsPath + "ConvertedFiles"
							+ File.separator + post + ".txt");
					BufferedWriter out = new BufferedWriter(
							new FileWriter(file), 32768);
					out.write(text);
					out.flush();
					out.close();
				}

			}
		}

	}

	private static String trimTextForTechnoSciFiNews(String text) {

		if (!text.contains("\n"))
			return "";

		while (text.startsWith("\n"))
			text = text.substring(text.indexOf("\n"));

		text = text.replaceAll("\n", "------newline------");
		text = text.replaceAll("\\s+", " ").trim();
		text = text.replaceAll("------newline------", "\n");

		String trimText = "Home | Glossary | Timeline | New";
		if (text.contains(trimText)) {
			if (text.split(trimText).length > 1)
				return text.split(trimText)[1];
			else
				return "";
		} else
			return text;
	}

	private static void TECHNO_Content_Extract() throws MalformedURLException,
			IOException {
		int totalPosts = 0;
		String blogsPath = techContentPath; //"C:\\xampp\\htdocs\\technovelgy\\content";
		File blogsDir = new File(blogsPath);
		String[] blogPosts = blogsDir.list();
		for (String post : blogPosts) {
			if (post.endsWith(".html")) {
				URL blogPostUrl = new URL(
						"http://localhost/technovelgy/content/" + post);
				if (checkResponse(blogPostUrl) == 404)
					continue;
				String text = "";
				try {
					text = DefaultExtractor.INSTANCE.getText(blogPostUrl);
				} catch (BoilerpipeProcessingException e) {
					e.printStackTrace();
				}

				text = trimTextForTechnoContent(text);

				String textTitle = "";
				try {
					textTitle = ArticleExtractor.INSTANCE.getText(blogPostUrl);
				} catch (BoilerpipeProcessingException e) {
					e.printStackTrace();
				}

				textTitle = extractTitleTechno(textTitle);
				if (textTitle.trim().equals("") || textTitle.trim().isEmpty()
						|| textTitle == null || textTitle.length() < 5)
					text = "";
				else
					text = textTitle + "\n" + text;

				if (!text.trim().isEmpty() && !text.trim().equals("")
						&& text.trim().length() > 2) {
					// text = text.trim().length()+" "+text;
					post = post.substring(0, post.lastIndexOf(".html"));
					File file = new File(blogsPath + "ConvertedFiles"
							+ File.separator + post + ".txt");
					BufferedWriter out = new BufferedWriter(
							new FileWriter(file), 32768);
					out.write(text);
					out.flush();
					out.close();
				}
			}
		}

	}

	private static String extractTitleTechno(String textTitle) {
		textTitle = textTitle.split("\n")[0];
		textTitle = textTitle.replaceAll("\\s+", " ").trim();
		if (textTitle.startsWith("Additional resources -"))
			return "";
		return textTitle;
	}

	private static String trimTextForTechnoContent(String text) {
		text = text.replaceAll("\n", "------newline------");
		text = text.replaceAll("\\s+", " ").trim();
		text = text.replaceAll("------newline------", "\n");
		String trimtext = "Want to Contribute an Item?";
		if (text.startsWith("-") && text.contains("\n")) {
			text = text.substring(text.indexOf("\n"));
		}
		if (text.contains(trimtext))
			text = text.split(trimtext)[0];
		trimtext = "'Over a radius of several miles";
		if (text.contains(trimtext))
			text = text.split(trimtext)[0];
		return text;
	}

	private static int checkResponse(URL blogPostUrl) throws IOException {
		URL u = blogPostUrl;
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("GET");
		huc.connect();
		int code = huc.getResponseCode();
		return code;
	}

	private static void SF_ENC_Extract() throws IOException {
		int totalPosts = 0;
		String blogsPath = sfePath;// "C:\\xampp\\htdocs\\sfe\\entry";
		File blogsDir = new File(blogsPath);
		String[] blogPosts = blogsDir.list();
		for (String post : blogPosts) {
			if (post.endsWith(".html") && !post.endsWith("-2.html")) {
				URL blogPostUrl = new URL("http://localhost/sfe/entry/" + post);
				if (checkResponse(blogPostUrl) == 404)
					continue;
				String text = "";
				try {
					text = CanolaExtractor.INSTANCE.getText(blogPostUrl);
				} catch (BoilerpipeProcessingException e) {
					e.printStackTrace();
				}

				text = trimTextSfEnc(text);

				if (!text.isEmpty()) {
					post = post.substring(0, post.lastIndexOf(".html"));
					File file = new File(blogsPath + "ConvertedFiles"
							+ File.separator + post + ".txt");
					BufferedWriter out = new BufferedWriter(
							new FileWriter(file), 32768);
					out.write(text);
					out.flush();
					out.close();
				}
			}
		}

	}

	private static String trimTextSfEnc(String text) {
		String trimText = "The SFE at Loncon 3\nWe passed a couple of major milestones on 1st August: the SFE is now over 4.5 million words, of which John Clute’s own contribution has now exceeded 2 million. (For comparison, the 1993 second edition was 1.3 million words, and … Continue reading →\n10,000\nWe’ve reached a couple of milestones recently. The SFE gallery of book covers now has more than 10,000 images: this one seemed appropriate for the 10,000th. Our series of slideshows of thematically linked covers has continued to grow, and Darren Nash of … Continue reading →\nThe Gallery\nWe’ve been talking for a while about new features to add to the SFE, and another one has gone live today: the Gallery, which collects together covers for sf books and links them back to SFE entries. To quote from … Continue reading →\n";
		if (text.contains(trimText))
			;
		text = text
				.split("The SFE at Loncon 3\nWe passed a couple of major milestones on 1st August:")[0];

		if (!text.contains("\n"))
			return "";
		if (text.split("\n").length == 1)
			return "";
		else if (text.split("\n").length == 2 && text.split("\n")[1].isEmpty())
			return "";
		if (!text.startsWith("Tagged")) {
			return "";
		}

		return text;
	}

	private static void TOR_Extract() throws IOException {
		int totalPosts = 0;
		String blogsPath = torPath;	//"C:\\xampp\\htdocs\\tor\\blogs";
		File blogsDir = new File(blogsPath);
		String[] years = blogsDir.list();

		for (String year : years) {

			File yearDir = new File(blogsPath + File.separator + year);

			String[] months = yearDir.list();

			for (String month : months) {

				File monthDir = new File(blogsPath + File.separator + year
						+ File.separator + month);
				if (!monthDir.getAbsoluteFile().toString().endsWith(".html")) {
					String[] blogPosts = monthDir.list();
					totalPosts += blogPosts.length;
					for (String post : blogPosts) {
						if (post.endsWith(".html")) {
							URL blogPostUrl = new URL(
									"http://localhost/tor/blogs/" + year + "/"
											+ month + "/" + post);

							if (checkResponse(blogPostUrl) == 404)
								continue;
							String text = "";
							try {
								text = ArticleExtractor.INSTANCE
										.getText(blogPostUrl);
							} catch (BoilerpipeProcessingException e) {
								e.printStackTrace();
								continue;
							}

							if (text.trim() != null && !text.equals("")) {
								String newfilename = post.substring(0,
										post.lastIndexOf(".html"));
								File file = new File(blogsPath
										+ "TempConvertedFiles" + File.separator
										+ newfilename + ".txt");
								BufferedWriter out = new BufferedWriter(
										new FileWriter(file), 32768);
								out.write(year + "/" + month + "\n" + text);
								out.flush();
								out.close();
							}
						}
					}
				}
			}

		}

	}

	private static void test() throws MalformedURLException,
			BoilerpipeProcessingException {
		URL url = new URL(
				"http://localhost/technovelgy/Technology-Article/Technology-Article0f91.html");

		String text = CanolaExtractor.INSTANCE.getText(url);
		text = text.replaceAll("\n", "------newline------");
		text = text.replaceAll("\\s+", " ").trim();

		if (text.contains("Home | Glossary | Timeline | New"))
			text = text.replace("Home | Glossary | Timeline | New", "");

		text = text.replaceAll("------newline------", "\n").trim();

	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}
