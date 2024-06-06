
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import oracle.security.o3logon.a;

// construct similar object
class SimilarObject {
	public String name;
	public double similarity;

	public SimilarObject(String name, double similarity) {
		this.name = name;
		this.similarity = similarity;
	}
}

// construct Information Retrieval object
class IR {
	public String m_address;
	public String truth;
	public String temp;
	public int tp;
	public int fp;
	public int tn;
	public int fn;
	public double maxsim;

	public IR(String m_address, String truth, String temp, int tp, int fp, int tn, int fn, double maxsim) {
		this.m_address = m_address;
		this.truth = truth;
		this.temp = temp;
		this.tp = tp;
		this.fp = fp;
		this.tn = tn;
		this.fn = fn;
		this.maxsim = 0;
	}

	public void clear() {
		this.m_address = null;
		this.truth = null;
		this.temp = null;
		this.tp = 0;
		this.fp = 0;
		this.tn = 0;
		this.fn = 0;
		this.maxsim = 0;
	}
}

// construct address object
class Address {
	public int id;
	public String longitude;
	public String latitude;
	public String address;
	public String country;
	public String city;
	public String street;

	public Address(int id, String longitude, String latitude, String address, String country, String city,
			String street) {
		this.id = id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.address = address;
		this.country = country;
		this.city = city;
		this.street = street;
	}
}

class PoiAddress {
	public int id;
	public Double longitude;
	public Double latitude;
	public String address;
	public String country;
	public String city;
	public String street;
	public int column;
	public int row;
	public int level;
	

	public PoiAddress(int id, Double longitude, Double latitude, String address, String country, String city,
			String street, int column, int row, int level) {
		this.id = id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.address = address;
		this.country = country;
		this.city = city;
		this.street = street;
		this.column = column;
		this.row = row;
		this.level = level;
	}

	public String getPoiAddress_Address() {
		return address;
	}

	public int getPoiAddress_Column() {
		return column;
	}

	public int getPoiAddress_Row() {
		return row;
	}

	public int getPoiAddress_Level() {
		return level;
	}

	public Double getPoiAddress_Longitude() {
		return longitude;
	}

	public Double getPoiAddress_Latitude() {
		return latitude;
	}
}

// construct code object
class Code {
	public int x;
	public int y;
	public int level;

	Code() {
	};

	public Code(int x, int y, int level) {
		this.x = x;
		this.y = y;
		this.level = level;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Code other = (Code) obj;
		if (level != other.level)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}

// construct POI object
class POI {
	public double x;
	public double y;
	public Code code;
	public String name;
	public String address;

	public POI(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public POI(double x, double y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
	}

	public POI(double x, double y, int level, int px, int py, String name, String address) {
		this.x = x;
		this.y = y;
		this.code = new Code(px, py, level);
		this.name = name;
		this.address = address;
	}

	public POI() {
		this.code = new Code();
	}
}

// Oracle database connection
class OracleConnection {
	private static String USERNAMR = "system";
	private static String PASSWORD = "123456";
	private static String DRVIER = "oracle.jdbc.OracleDriver";
	private static String URL = "jdbc:oracle:thin:@localhost:1521:ORCLL";

	static Connection connection = null;
	static ResultSet rs = null;

	public static Connection getConnection() {
		try {
			Class.forName(DRVIER);
			connection = DriverManager.getConnection(URL, USERNAMR, PASSWORD);
			System.out.println("success connection!");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class not find !", e);
		} catch (SQLException e) {
			throw new RuntimeException("get connection error!", e);
		}
		return connection;
	}

	public static ResultSet SelectData(PreparedStatement pstm) {
		try {
			rs = pstm.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
}

// Text similarity
class TextSimilarity {
	// compute Levenshtein distance
	public static int LevenshteinDistance(String s, String t) {
		int d[][];
		int sLen = s.length();
		int tLen = t.length();
		int si;
		int ti;
		char ch1;
		char ch2;
		int cost;
		if (sLen == 0) {
			return tLen;
		}
		if (tLen == 0) {
			return sLen;
		}
		d = new int[sLen + 1][tLen + 1];
		for (si = 0; si <= sLen; si++) {
			d[si][0] = si;
		}
		for (ti = 0; ti <= tLen; ti++) {
			d[0][ti] = ti;
		}
		for (si = 1; si <= sLen; si++) {
			ch1 = s.charAt(si - 1);
			for (ti = 1; ti <= tLen; ti++) {
				ch2 = t.charAt(ti - 1);
				if (ch1 == ch2) {
					cost = 0;
				} else {
					cost = 1;
				}
				d[si][ti] = Math.min(Math.min(d[si - 1][ti] + 1, d[si][ti - 1] + 1), d[si - 1][ti - 1] + cost);
			}
		}
		return d[sLen][tLen];
	}

	// compute text similarity of whole string
	public static double textsimilarity(String src, String tar) {
		int ld = LevenshteinDistance(src, tar);
		return 1 - (double) ld / Math.max(src.length(), tar.length());
	}

	// compute text similarity of string which has abbreviation
	public static double textsimilarity2(String[] tar_list, String[] src_list, Map<String, String> abb_map) {
		for (int i = 0; i < tar_list.length; i++) {
			if (abb_map.containsKey(tar_list[i])) {
				tar_list[i] = abb_map.get(tar_list[i]);
			}
		}
		for (int i = 0; i < src_list.length; i++) {
			if (abb_map.containsKey(src_list[i])) {
				src_list[i] = abb_map.get(src_list[i]);
			}
		}
		String tar = tar_list[0];
		for (int i = 1; i < tar_list.length; i++) {
			tar = tar + "_" + tar_list[i];
		}
		String src = src_list[0];
		for (int i = 1; i < src_list.length; i++) {
			src = src + "_" + src_list[i];
		}
		double sim_total = textsimilarity(src, tar);
		return sim_total;
	}
}

class SemanticSimilarity {
	// extract numbers from string
	public static String getNumbers(String content) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}

	// to know whether the numbers are odd or even, if both are odd or even, return
	// 0, else return 10
	// odd house number is on one side of the street, even house number is on the
	// other side of the street
	public static int odd(int tar, int src) {
		if (tar % 2 == 0 && src % 2 == 0) {
			return 0;
		} else if (tar % 2 == 1 && src % 2 == 1) {
			return 0;
		} else {
			return 10;
		}
	}

	// compute semantic similarity of two strings
	// use WordNet to compute semantic similarity
	// use Wu and Palmer method to compute similarity
	// if the word is not in WordNet, use abbreviation map to find the abbreviation
	// of the word
	// if the abbreviation is in WordNet, use the abbreviation to compute similarity
	// if the abbreviation is not in WordNet, use the original word to compute
	// similarity
	// if the word is not in WordNet and the abbreviation is not in WordNet, return
	// 0
	// if the word is in WordNet and the abbreviation is not in WordNet, return the
	// similarity of the word
	// if the word is not in WordNet and the abbreviation is in WordNet, return the
	// similarity of the abbreviation
	public static double src2tar(String label_tar, String label_src, Map<String, String> abb_map, IDictionary dict) {
		double sim_src = 0;
		IIndexWord localIIndexWord1 = dict.getIndexWord(label_tar, POS.NOUN);
		if (localIIndexWord1 != null) {
			IIndexWord localIIndexWord2 = dict.getIndexWord(label_src, POS.NOUN);
			if (localIIndexWord2 != null) {
				List<IWordID> localList1 = localIIndexWord1.getWordIDs();
				List<IWordID> localList2 = localIIndexWord2.getWordIDs();
				sim_src = WuAndPalmer.wup(localList1, localList2, "n");
			} else if (abb_map.containsKey(label_src)) {
				List<IWordID> localList1 = localIIndexWord1.getWordIDs();
				localIIndexWord2 = dict.getIndexWord(abb_map.get(label_src), POS.NOUN);
				List<IWordID> localList2 = localIIndexWord2.getWordIDs();
				sim_src = WuAndPalmer.wup(localList1, localList2, "n");
			}
		} else if (abb_map.containsKey(label_tar)) {
			IIndexWord localIIndexWord2 = dict.getIndexWord(label_src, POS.NOUN);
			if (localIIndexWord2 != null) {
				localIIndexWord1 = dict.getIndexWord(abb_map.get(label_tar), POS.NOUN);
				List<IWordID> localList1 = localIIndexWord1.getWordIDs();
				List<IWordID> localList2 = localIIndexWord2.getWordIDs();
				sim_src = WuAndPalmer.wup(localList1, localList2, "n");
			} else if (abb_map.containsKey(label_src)) {
				localIIndexWord1 = dict.getIndexWord(abb_map.get(label_tar), POS.NOUN);
				List<IWordID> localList1 = localIIndexWord1.getWordIDs();
				localIIndexWord2 = dict.getIndexWord(abb_map.get(label_src), POS.NOUN);
				List<IWordID> localList2 = localIIndexWord2.getWordIDs();
				sim_src = WuAndPalmer.wup(localList1, localList2, "n");
			}
		}
		return sim_src;
	}

	// compute semantic similarity of two lists of strings
	// use src2tar to compute similarity of each pair of strings
	// use the maximum similarity of each pair of strings to compute the similarity
	// of the two lists of strings
	public static double srclist2tarlist(String[] tar_list, String[] src_list, Map<String, String> abb_map,
			IDictionary dict, String str) {
		double semsim_total = 0;
		String[] min_list;
		String[] max_list;
		min_list = tar_list;
		max_list = src_list;
		for (int i = 0; i < min_list.length; i++) {
			double sim_tar = 0;
			for (int j = 0; j < max_list.length; j++) {
				double sim_src = 0;
				if (!min_list[i].equals("") && !max_list[j].equals("")) {
					sim_src = src2tar(min_list[i], max_list[j], abb_map, dict);
					if (sim_tar < sim_src)
						sim_tar = sim_src;
				}
			}
			semsim_total = semsim_total + sim_tar;
		}
		return semsim_total / Math.max(tar_list.length, src_list.length);
	}

	// The spatialsim method is used to calculate the spatial similarity between two
	// arrays of strings. It first designates the shorter array as min_list and the
	// longer array as max_list, then extracts each element in the min_list as a
	// number and compares it one by one with the elements in the max_list. In the
	// process of comparison, the number is extracted first, and the parity
	// difference of the two numbers is calculated, and then the spatial similarity
	// is calculated according to a certain formula, and the maximum value is
	// selected as the final similarity value.
	public static double spatialsim(String[] tar_list, String[] src_list, Map<String, String> abb_map, IDictionary dict,
			String str) {
		double spatialsim = 0;
		String[] min_list;
		String[] max_list;
		min_list = tar_list;
		max_list = src_list;
		for (int i = 0; i < min_list.length; i++) {
			for (int j = 0; j < max_list.length; j++) {
				if (!min_list[i].equals("") && !max_list[j].equals("")) {
					String tar_str = getNumbers(min_list[i]);
					String[] src_str_list = max_list[j].split("-");
					double sim = 0;
					for (int n = 0; n < src_str_list.length; n++) {
						String src_str = getNumbers(src_str_list[n]);
						if (!tar_str.equals("") && !src_str.equals("")) {
							int num_tar = Integer.valueOf(tar_str);
							int num_src = Integer.valueOf(src_str);
							int odd = odd(num_tar, num_src);
							sim = (double) num_tar / (num_tar + Math.abs(num_tar - num_src) + odd);
							if (spatialsim < sim) {
								spatialsim = sim;
							}
						}
					}
				}
			}
		}
		return spatialsim;
	}
}

// Read and write files
class FileOperator {
	// compute the size of the direction set, the compact rate and the spatial
	// resolution
	// of the direction set
	public static void computeParameters() throws IOException {
		long size1 = 0;
		long size2 = 0;
		long size3 = 0;

		// compute posible directions
		ArrayList<String> poi_list = FileOperator.readURI("POIs_D1.txt");
		size1 = (long) poi_list.size() * (poi_list.size() - 1);
		FileOperator.writeFile("Fogliaroni's method: " + size1, "sizeofdirection_D1.txt");

		// compute the compact rate and the spatial resolution of the direction set
		for (int i = 0; i < 28; i++) {
			ArrayList<String> code_list = FileOperator.readURI(i + "_code_D1.txt");
			size2 = (long) code_list.size() * (code_list.size() - 1);
			double compactrate = (float) size2 / size1;
			double resolution = 3.14 * 6371000 / Math.pow(2, i);
			String str_resolution = String.format("%.2f", resolution);
			FileOperator.writeFile("Scale-" + i + ": " + size2, "sizeofdirection_D1.txt");
			FileOperator.writeFile("Scale-" + i + ": " + compactrate, "compactrate_D1.txt");
			FileOperator.writeFile("Scale-" + i + ": " + str_resolution, "spatialresolution_D1.txt");
		}

		// compute the size of the direction set and the compact rate of the direction
		ArrayList<String> directions = FileOperator.readURI("snew_D1.txt");
		size3 = (long) size3 + directions.size();
		for (int i = 5; i < 28; i++) {
			directions = FileOperator.readURI(i + "_dirc_D1.txt");
			size3 = size3 + directions.size();
		}
		size3 = 2 * size3;
		float compactrate = (float) size3 / size1;
		FileOperator.writeFile("proposed  algorithm: " + size3, "sizeofdirection_D1.txt");
		FileOperator.writeFile("proposed  algorithm: " + compactrate, "compactrate_D1.txt");
	}

	// semantic similarity
	// compute the similarity of the address set and the truth set
	// compute precision, recall and F-measure
	public static void getMatchIR(String pathname) {
		ArrayList<String> truthlist = FileOperator.readURI("matchedtruth_D1.txt");
		IR ir = new IR(null, null, null, 0, 0, 0, 0, 0);
		IR ir_total = new IR(null, null, null, 0, 0, 0, 0, 0);
		String str_out = null;
		try (FileReader reader = new FileReader(pathname);
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			int lines = 0;
			while ((line = br.readLine()) != null) {
				if (line.contains("Match")) {
					String[] strarray = line.split(" ");
					String match_address = strarray[1];
					double similarity = Double.valueOf(strarray[2]);
					if (!match_address.equals(ir.temp)) {
						if (similarity >= ir.maxsim) {
							ir.maxsim = similarity;
							ir.temp = match_address;
							if (match_address.equals(ir.truth)) {
								ir.tp = ir.tp + 1;
							} else {
								ir.fp = ir.fp + 1;
							}
						}
					}
				} else {
					if (lines > 0) {
						ir.fn = 1 - ir.tp;
						ir_total.tp = ir_total.tp + ir.tp;
						ir_total.fp = ir_total.fp + ir.fp;
						ir_total.fn = ir_total.fn + ir.fn;
						ir.clear();
					}
					ir.m_address = line;
					ir.truth = truthlist.get(lines);
					lines++;
				}
			}
			ir.fn = 1 - ir.tp;
			ir_total.tp = ir_total.tp + ir.tp;
			ir_total.fp = ir_total.fp + ir.fp;
			ir_total.fn = ir_total.fn + ir.fn;
			double precision = (double) ir_total.tp / (ir_total.tp + ir_total.fp);
			double recall = (double) ir_total.tp / (ir_total.tp + ir_total.fn);
			double f = 2.0 * precision * recall / (precision + recall);
			str_out = precision + " " + recall + " " + f;
			String[] strlist = pathname.split("\\\\");
			String str = strlist[strlist.length - 1];
			FileOperator.writeFile(str + ": " + str_out, "matchir_D1.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// spatial similarity
	// compute the similarity of the address set and the truth set
	// compute precision, recall and F-measure
	public static void getReasonIR() {
		IR ir = new IR(null, null, null, 0, 0, 0, 0, 0);
		double precision = 0;
		double recall = 0;
		double f = 0;
		String str_out = null;
		int truthsize = 0;
		int targetsize = 0;
		try (FileReader reader = new FileReader("reasoningresult_D1.txt");
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			ArrayList<String> target = new ArrayList<String>();
			ArrayList<String> truth = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				if (line.contains("discription")) {
					truthsize = truth.size();
					targetsize = target.size();
					if (truthsize != 0 && targetsize != 0) {
						target.retainAll(truth);
						ir.tp = ir.tp + target.size();
						ir.fp = ir.fp + targetsize - target.size();
						ir.fn = ir.fn + truthsize - target.size();
					}
					target.clear();
					truth.clear();
				} else if (line.contains("target:")) {
					String[] str = line.split(" ");
					target.add(str[1]);
				} else if (line.contains("truth:")) {
					String[] str = line.split(" ");
					truth.add(str[1]);
				}
			}
			truthsize = truth.size();
			targetsize = target.size();
			target.retainAll(truth);
			ir.tp = ir.tp + target.size();
			ir.fp = ir.fp + targetsize - target.size();
			ir.fn = ir.fn + truthsize - target.size();
			precision = (double) ir.tp / (ir.tp + ir.fp);
			recall = (double) ir.tp / (ir.tp + ir.fn);
			f = (double) 2.0 * precision * recall / (precision + recall);
			str_out = precision + " " + recall + " " + f;
			FileOperator.writeFile(str_out, "reasoningIR_D1.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// compute the average time of the spatial similarity
	public static void getAvgReasonT() {
		double targettime = 0;
		double truthtime = 0;
		int num = 0;
		String str_out = null;
		try (FileReader reader = new FileReader("reasontime_D1.txt");
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("proposed:")) {
					String[] str = line.split(" ");
					targettime = targettime + Double.valueOf(str[1]);
					num = num + 1;
				} else if (line.contains("general:")) {
					String[] str = line.split(" ");
					truthtime = truthtime + Double.valueOf(str[1]);
				}
			}
			targettime = targettime / num;
			truthtime = truthtime / num;
			str_out = targettime + " " + truthtime;
			FileOperator.writeFile(str_out, "reasonavgtime_D1.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// read the URI from the file
	public static ArrayList<String> readURI(String pathname) {
		ArrayList<String> uris = new ArrayList<String>();
		try (FileReader reader = new FileReader(pathname);
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				uris.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uris;
	}

	// read the URI from the file
	// store them in a hashset
	public static HashSet<Code> getcodelist(int i) throws IOException {
		String pathname = i + "_code_D1.txt";
		HashSet<Code> code_set = new HashSet<Code>();
		try (FileReader reader = new FileReader(pathname);
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] strarray = line.split("_");
				String level = strarray[0];
				String x = strarray[1];
				String y = strarray[2];
				Code c = new Code(Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(level));
				code_set.add(c);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return code_set;
	}

	// write the URI to the file
	public static void writeFile(String str, String path) {
		try (FileWriter writer = new FileWriter(path, true); BufferedWriter out = new BufferedWriter(writer)) {
			out.write(str + "\r\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

// construct Locating class
public class Locating {
	static int targetpois;
	static double similarity_max;

	static ArrayList<HashSet<Code>> initmulticode = new ArrayList<HashSet<Code>>();
	static ArrayList<SimilarObject> address_bestlist = new ArrayList<>();
	static ArrayList<SimilarObject> country_bestlist = new ArrayList<>();
	static ArrayList<SimilarObject> city_bestlist = new ArrayList<>();
	static ArrayList<SimilarObject> street_bestlist = new ArrayList<>();
	static ArrayList<SimilarObject> doorplate_bestlist = new ArrayList<>();

	static Connection conn;
	static PreparedStatement pstm;
	static IDictionary dict;
	static ResultSet rs;

	static Map<String, String> abb_map = new HashMap<String, String>();

	// initialize wordnet
	public static IDictionary InitWordNet() throws IOException {
		String path = "C:/2-Documents/project/wordnet/dict";
		URL url = new URL("file", null, path);
		IDictionary dict = new Dictionary(url);
		dict.open();
		return dict;
	}

	// sort the list of similar objects
	public static void selectSort(ArrayList<SimilarObject> list) {
		for (int i = 0; i < list.size(); i++) {
			int k = i;
			for (int j = list.size() - 1; j > i; j--) {
				if (list.get(j).similarity > list.get(k).similarity) {
					k = j;
				}
			}
			SimilarObject temp = list.get(i);
			list.set(i, list.get(k));
			list.set(k, temp);
		}
	}

	// select the maximum similarity from the list of similar objects
	public static ArrayList<SimilarObject> selectMax(ArrayList<SimilarObject> list) {
		ArrayList<SimilarObject> maxlist = new ArrayList<SimilarObject>();
		double max = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).similarity == max && maxlist.size() > 0) {
				maxlist.add(list.get(i));
			} else if (list.get(i).similarity > max) {
				maxlist.clear();
				max = list.get(i).similarity;
				maxlist.add(list.get(i));
			}
		}
		return maxlist;
	}

	// due to lantitude and longitude, compute the code of the location
	public static Code getC(double longitude, double latitude, int level) {
		int x = (int) (Math.floor((Math.pow(2, level) * (Double.valueOf(longitude) + 180)) / 180)
				% Math.pow(2, level + 1));
		int y = (int) (Math.floor((Math.pow(2, level - 1) * (Double.valueOf(latitude) + 90)) / 90)
				% Math.pow(2, level));
		Code c = new Code(x, y, level);
		return c;
	}

	// compute code of given scale
	public static void computeCodes(int scale) throws IOException {
		ArrayList<String> str_list = FileOperator.readURI("POIs_D1.txt");
		HashSet<String> codelist = new HashSet<String>();
		for (int i = 0; i < str_list.size(); i++) {
			String[] strlist = str_list.get(i).split(" ");
			double longitude = Double.valueOf(strlist[0]);
			double latitude = Double.valueOf(strlist[1]);
			Code c = getC(longitude, latitude, scale);
			String s = c.level + "_" + c.x + "_" + c.y;
			codelist.add(s);
		}
		String path = scale + "_code_D1.txt";
		for (String triple : codelist) {
			FileOperator.writeFile(triple, path);
		}
	}

	// given level
	// compute neighbour code of the given code at NorthEast and NorthWest
	public static void computeSNEW(int level) throws IOException {
		HashSet<Code> codelist = FileOperator.getcodelist(level);
		HashSet<String> stringvec = new HashSet<String>();
		String path = "snew_D1.txt";
		for (Code c1 : codelist) {
			for (Code c2 : codelist) {
				if (c1.x > c2.x && c1.y == c2.y) {
					String s = c1.level + "_" + c1.x + "_" + c1.y + " E " + c2.level + "_" + c2.x + "_" + c2.y;
					stringvec.add(s);
				} else if (c1.x == c2.x && c1.y > c2.y) {
					String s = c1.level + "_" + c1.x + "_" + c1.y + " N " + c2.level + "_" + c2.x + "_" + c2.y;
					stringvec.add(s);
				}
			}
		}
		for (String str : stringvec) {
			FileOperator.writeFile(str, path);
		}
	}

	// given level
	// compute neighbour code of the given code at NorthEast and NorthWest
	public static void computeDirection(int level) throws IOException {
		HashSet<Code> codelist = FileOperator.getcodelist(level);
		int colmin = Integer.MAX_VALUE;
		int colmax = Integer.MIN_VALUE;
		int rowmin = Integer.MAX_VALUE;
		int rowmax = Integer.MIN_VALUE;
		HashSet<String> stringvec = new HashSet<String>();
		String path = level + "_dirc_D1.txt";

		for (Code code : codelist) {
			if (code.x > colmax)
				colmax = code.x;
			if (code.x < colmin)
				colmin = code.x;
			if (code.y > rowmax)
				rowmax = code.y;
			if (code.y < rowmin)
				rowmin = code.y;
		}
		if (colmin < colmax && rowmin < rowmax) {
			for (int i = rowmin; i < rowmax; i++) {
				if (i / 2 == (i + 1) / 2) {
					Vector<Code> codes_down = new Vector<Code>();
					Vector<Code> codes_up = new Vector<Code>();
					for (Code code : codelist) {
						if (code.y == i)
							codes_down.add(code);
						if (code.y == i + 1)
							codes_up.add(code);
					}
					if (codes_down.size() != 0 && codes_up.size() != 0) { //whether have both up and down rows
						for (int coli = 0; coli < codes_down.size(); coli++) {
							for (int colj = 0; colj < codes_up.size(); colj++) {
								if (codes_up.get(colj).x > codes_down.get(coli).x) {
									String s = codes_up.get(colj).level + "_" + codes_up.get(colj).x + "_"
											+ codes_up.get(colj).y + " NE " + codes_down.get(coli).level + "_"
											+ codes_down.get(coli).x + "_" + codes_down.get(coli).y;
									stringvec.add(s);
								} else if (codes_up.get(colj).x < codes_down.get(coli).x) {
									String s = codes_up.get(colj).level + "_" + codes_up.get(colj).x + "_"
											+ codes_up.get(colj).y + " NW " + codes_down.get(coli).level + "_"
											+ codes_down.get(coli).x + "_" + codes_down.get(coli).y;
									stringvec.add(s);
								}
							}
						}
					}
				}
			}
			for (int i = colmin; i < colmax; i++) {
				if (i / 2 == (i + 1) / 2) {
					Vector<Code> codes_left = new Vector<Code>();
					Vector<Code> codes_right = new Vector<Code>();
					for (Code code : codelist) {
						if (code.x == i)
							codes_left.add(code);
						if (code.x == i + 1)
							codes_right.add(code);
					}
					if (codes_left.size() != 0 && codes_right.size() != 0) {
						for (int rowi = 0; rowi < codes_left.size(); rowi++) {
							for (int rowj = 0; rowj < codes_right.size(); rowj++) {
								if (codes_right.get(rowj).y > codes_left.get(rowi).y) {
									if (codes_right.get(rowj).y / 2 != codes_left.get(rowi).y / 2) {
										String s = codes_right.get(rowj).level + "_" + codes_right.get(rowj).x + "_"
												+ codes_right.get(rowj).y + " NE " + codes_left.get(rowi).level + "_"
												+ codes_left.get(rowi).x + "_" + codes_left.get(rowi).y;
										stringvec.add(s);
									}
								}
							}
						}
						for (int rowi = 0; rowi < codes_right.size(); rowi++) {
							for (int rowj = 0; rowj < codes_left.size(); rowj++) {
								if (codes_left.get(rowj).y > codes_right.get(rowi).y) {
									if (codes_right.get(rowi).y / 2 != codes_left.get(rowj).y / 2) {
										String s = codes_left.get(rowj).level + "_" + codes_left.get(rowj).x + "_"
												+ codes_left.get(rowj).y + " NW " + codes_right.get(rowi).level + "_"
												+ codes_right.get(rowi).x + "_" + codes_right.get(rowi).y;
										stringvec.add(s);
									}
								}
							}
						}
					}
				}
			}
		}
		for (String str : stringvec) {
			FileOperator.writeFile(str, path);
		}
	}

	// convert geometrical information to RDF triples
	public static void getTrialInstanceTriple() throws IOException {
		ArrayList<String> str_list = FileOperator.readURI("POIs_D1.txt");

		ArrayList<Address> list = new ArrayList<Address>();
		for (int i = 0; i < str_list.size(); i++) {
			String[] strlist = str_list.get(i).split(" ");
			String longitude = strlist[0];
			String latitude = strlist[1];
			String address = strlist[2];
			String country = strlist[3];
			String city = strlist[4];
			String street = strlist[5];
			Address a = new Address(i + 1, longitude, latitude, address, country, city, street);
			list.add(a);
		}
		System.out.println("list size: " + list.size());
		int outputIteration = 0;
		for (Address a : list) {
			outputIteration = outputIteration + 1;
			if (outputIteration % 1000 == 0) {
				System.out.println("outputIteration: " + outputIteration);
			}
			ArrayList<String> triples1 = new ArrayList<String>();
			String poi = "http://www.poi.org/poi#poi/" + a.id; // poi
			String spatialinfo = "http://www.poi.org/poi#spatialinfo/" + a.id;
			String spatialthing = "http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing/" + a.id;
			String semanticinfo = "http://www.poi.org/poi#semanticinfo/" + a.id;
			String address = "http://www.poi.org/poi#address/" + a.address;
			String country = "http://www.poi.org/poi#country/" + a.country;
			String city = "http://www.poi.org/poi#city/" + a.city;
			String street = "http://www.poi.org/poi#street/" + a.street;
			String longitude = "\"" + a.longitude + "\"";
			String latitude = "\"" + a.latitude + "\"";
			String poi_triple = "<" + poi
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#POI>";
			triples1.add(poi_triple);
			String spatialinfo_triple = "<" + spatialinfo
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#SpatialInfo>";
			triples1.add(spatialinfo_triple);
			String spatialrefenrce_triple = "<" + spatialthing
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#SpatialThing>";
			triples1.add(spatialrefenrce_triple);
			String semanticinfo_triple = "<" + semanticinfo
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#SemanticInfo>";
			triples1.add(semanticinfo_triple);
			String address_triple = "<" + address
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Address>";
			triples1.add(address_triple);
			String hasspatial_triple = "<" + poi + "> <http://www.poi.org/poi#hasSpatial> <" + spatialinfo + ">";
			triples1.add(hasspatial_triple);
			String hasref_triple = "<" + spatialinfo + "> <http://www.poi.org/poi#hasRef> <" + spatialthing + ">";
			triples1.add(hasref_triple);
			String hassemantic_triple = "<" + poi + "> <http://www.poi.org/poi#hasSemantic> <" + semanticinfo + ">";
			triples1.add(hassemantic_triple);
			String hasaddress_triple = "<" + semanticinfo + "> <http://www.poi.org/poi#hasAddress> <" + address + ">";
			triples1.add(hasaddress_triple);
			if (a.country != "null") {
				String country_triple = "<" + country
						+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Country>";
				triples1.add(country_triple);
				String hascountry_triple = "<" + address + "> <http://www.poi.org/poi#hasCountry> <" + country + ">";
				triples1.add(hascountry_triple);
			}
			if (a.city != "null") {
				String city_triple = "<" + city
						+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#City>";
				triples1.add(city_triple);
				String hascity_triple = "<" + address + "> <http://www.poi.org/poi#hasCity> <" + city + ">";
				triples1.add(hascity_triple);
			}
			if (a.street != "null") {
				String street_triple = "<" + street
						+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Street>";
				triples1.add(street_triple);
				String hasstreet_triple = "<" + address + "> <http://www.poi.org/poi#hasStreet> <" + street + ">";
				triples1.add(hasstreet_triple);
			}
			String haslongitude_triple = "<" + spatialthing + "> <http://www.w3.org/2003/01/geo/wgs84_pos#long> "
					+ longitude;
			triples1.add(haslongitude_triple);
			String haslatitude_triple = "<" + spatialthing + "> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> "
					+ latitude;
			triples1.add(haslatitude_triple);
			Code trialCode = getC(Double.valueOf(a.longitude), Double.valueOf(a.latitude), 27);
			String code = "http://www.poi.org/poi#code/" + trialCode.level + "_" + trialCode.x + "_" + trialCode.y;
			String hascode_triple = "<" + spatialinfo + "> <http://www.poi.org/poi#hasCode> <" + code + ">";
			triples1.add(hascode_triple);
			String endSign = "<end>";
			triples1.add(endSign);
			for (String str : triples1) {
				FileOperator.writeFile(str, "trial_triples_D1.txt");
			}
		}
	}

	// convert geometrical information to RDF triples
	public static void getInstanceTriple() throws IOException {
		ArrayList<String> str_list = FileOperator.readURI("POIs_D1.txt");

		ArrayList<Address> list = new ArrayList<Address>();
		for (int i = 0; i < str_list.size(); i++) {
			String[] strlist = str_list.get(i).split(" ");
			String longitude = strlist[0];
			String latitude = strlist[1];
			String address = strlist[2];
			String country = strlist[3];
			String city = strlist[4];
			String street = strlist[5];
			Address a = new Address(i + 1, longitude, latitude, address, country, city, street);
			list.add(a);
		}
		System.out.println("list size: " + list.size());
		int outputIteration = 0;
		for (Address a : list) {
			outputIteration = outputIteration + 1;
			if (outputIteration % 1000 == 0) {
				System.out.println("outputIteration: " + outputIteration);
			}
			ArrayList<String> triples1 = new ArrayList<String>();
			String poi = "http://www.poi.org/poi#poi/" + a.id; // poi
			String spatialinfo = "http://www.poi.org/poi#spatialinfo/" + a.id;
			String spatialthing = "http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing/" + a.id;
			String semanticinfo = "http://www.poi.org/poi#semanticinfo/" + a.id;
			String address = "http://www.poi.org/poi#address/" + a.address;
			String country = "http://www.poi.org/poi#country/" + a.country;
			String city = "http://www.poi.org/poi#city/" + a.city;
			String street = "http://www.poi.org/poi#street/" + a.street;
			String longitude = "\"" + a.longitude + "\"";
			String latitude = "\"" + a.latitude + "\"";
			String poi_triple = "<" + poi
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#POI>";
			triples1.add(poi_triple);
			String spatialinfo_triple = "<" + spatialinfo
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#SpatialInfo>";
			triples1.add(spatialinfo_triple);
			String spatialreference_triple = "<" + spatialthing
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#SpatialThing>";
			triples1.add(spatialreference_triple);
			String semanticinfo_triple = "<" + semanticinfo
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#SemanticInfo>";
			triples1.add(semanticinfo_triple);
			String address_triple = "<" + address
					+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Address>";
			triples1.add(address_triple);
			String hasspatial_triple = "<" + poi + "> <http://www.poi.org/poi#hasSpatial> <" + spatialinfo + ">";
			triples1.add(hasspatial_triple);
			String hasref_triple = "<" + spatialinfo + "> <http://www.poi.org/poi#hasRef> <" + spatialthing + ">";
			triples1.add(hasref_triple);
			String hassemantic_triple = "<" + poi + "> <http://www.poi.org/poi#hasSemantic> <" + semanticinfo + ">";
			triples1.add(hassemantic_triple);
			String hasaddress_triple = "<" + semanticinfo + "> <http://www.poi.org/poi#hasAddress> <" + address + ">";
			triples1.add(hasaddress_triple);
			if (a.country != "null") {
				String country_triple = "<" + country
						+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Country>";
				triples1.add(country_triple);
				String hascountry_triple = "<" + address + "> <http://www.poi.org/poi#hasCountry> <" + country + ">";
				triples1.add(hascountry_triple);
			}
			if (a.city != "null") {
				String city_triple = "<" + city
						+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#City>";
				triples1.add(city_triple);
				String hascity_triple = "<" + address + "> <http://www.poi.org/poi#hasCity> <" + city + ">";
				triples1.add(hascity_triple);
			}
			if (a.street != "null") {
				String street_triple = "<" + street
						+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Street>";
				triples1.add(street_triple);
				String hasstreet_triple = "<" + address + "> <http://www.poi.org/poi#hasStreet> <" + street + ">";
				triples1.add(hasstreet_triple);
			}
			String haslongitude_triple = "<" + spatialthing + "> <http://www.w3.org/2003/01/geo/wgs84_pos#long> "
					+ longitude;
			triples1.add(haslongitude_triple);
			String haslatitude_triple = "<" + spatialthing + "> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> "
					+ latitude;
			triples1.add(haslatitude_triple);
			for (String str : triples1) {
				FileOperator.writeFile(str, "triples_D1.txt");
			}
			for (int i = 0; i < 28; i++) {
				Code c = getC(Double.valueOf(a.longitude), Double.valueOf(a.latitude), i);
				String code = "http://www.poi.org/poi#code/" + c.level + "_" + c.x + "_" + c.y;
				String hascode_triple = "<" + spatialinfo + "> <http://www.poi.org/poi#hasCode> <" + code + ">";
				FileOperator.writeFile(hascode_triple, i + "_code_triples_D1.txt");
			}
		}
	}

	// generate the code of triples
	// mark1
	public static void getCodeTriples() throws IOException {
		for (int i = 0; i < 28; i++) {
			System.out.println(i);
			HashSet<String> triples = new HashSet<String>();
			String pathname = i + "_code_D1.txt";
			String path = i + "_code_triples_D1.txt";
			try (FileReader reader = new FileReader(pathname);
					BufferedReader br = new BufferedReader(reader)) {
				String line;
				while ((line = br.readLine()) != null) {
					String code = "http://www.poi.org/poi#code/" + line;
					String code_triple = "<" + code
							+ "> <http://www.w3.org/1999/02/22-rdf-syntaxns#type> <http://www.poi.org/poi#Code>";
					String scale = "\"" + i + "\"";
					String hasscale_triple = "<" + code + "> <http://www.poi.org/poi#hasScale> " + scale;
					triples.add(code_triple);
					triples.add(hasscale_triple);
				}
				for (String triple : triples) {
					FileOperator.writeFile(triple, path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// generate RDF triples of the direction set
	public static void getDirectionTriples() throws IOException {
		for (int i = 5; i < 28; i++) {
			System.out.println("-" + i);
			HashSet<String> triples = new HashSet<String>();
			String pathname = i + "_dirc_D1.txt";
			String path = i + "_dirc_triples_D1.txt";
			try (FileReader reader = new FileReader(pathname);
					BufferedReader br = new BufferedReader(reader)) {
				String line;
				while ((line = br.readLine()) != null) {
					String[] strarray = line.split(" ");
					String code1 = "http://www.poi.org/poi#code/" + strarray[0];
					String direction = strarray[1];
					String code2 = "http://www.poi.org/poi#code/" + strarray[2];
					String direction_triple = "<" + code1 + "> <http://www.poi.org/poi#" + direction + "> <" + code2
							+ ">";
					triples.add(direction_triple);
				}
				for (String triple : triples) {
					FileOperator.writeFile(triple, path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String pathname = "snew_D1.txt";
		String path = "snew_triples_D1.txt";
		try (FileReader reader = new FileReader(pathname);
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			HashSet<String> triples = new HashSet<String>();
			while ((line = br.readLine()) != null) {
				String[] strarray = line.split(" ");
				String code1 = "http://www.poi.org/poi#code/" + strarray[0];
				String direction = strarray[1];
				String code2 = "http://www.poi.org/poi#code/" + strarray[2];
				String direction_triple = "<" + code1 + "> <http://www.poi.org/poi#" + direction + "> <" + code2 + ">";
				triples.add(direction_triple);
			}
			for (String triple : triples) {
				FileOperator.writeFile(triple, path);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// mix text similarity, semantic similarity and spatial similarity
	static void SimilarityMatch(String tar, String src, String[] tar_list, String[] src_list,
			Map<String, String> abb_map, IDictionary dict, ArrayList<SimilarObject> list, String str, double w1,
			double w2, double w3) throws IOException {

		double textsim_pname = TextSimilarity.textsimilarity2(tar_list, src_list, abb_map);

		double semsim_pname = SemanticSimilarity.srclist2tarlist(tar_list, src_list, abb_map, dict, str);

		double spasim_pname = SemanticSimilarity.spatialsim(tar_list, src_list, abb_map, dict, str);

		double sim_total = 0;

		if (textsim_pname == 1) {
			sim_total = textsim_pname;
		} else {
			sim_total = textsim_pname * w1 + semsim_pname * w2 + spasim_pname * w3;
		}

		SimilarObject so = new SimilarObject(str, sim_total);
		list.add(so);
	}

	// text simiilarity
	static void SimilarityString(String tar, String src, String[] tar_list, String[] src_list,
			Map<String, String> abb_map, ArrayList<SimilarObject> list, String str) throws IOException {
		double sim_total = TextSimilarity.textsimilarity2(tar_list, src_list, abb_map);
		SimilarObject so = new SimilarObject(str, sim_total);
		list.add(so);
	}

	// text similarity and semantic similarity
	static void SimilarityStringWup(String tar, String src, String[] tar_list, String[] src_list,
			Map<String, String> abb_map, IDictionary dict, ArrayList<SimilarObject> list, String str)
			throws IOException {
		double textsim_pname = TextSimilarity.textsimilarity2(tar_list, src_list, abb_map);
		double semsim_pname = SemanticSimilarity.srclist2tarlist(tar_list, src_list, abb_map, dict, str);
		double sim_total = 0;
		if (textsim_pname == 1) {
			sim_total = textsim_pname;
		} else {
			sim_total = textsim_pname * 0.5 + semsim_pname * 0.5;
		}
		SimilarObject so = new SimilarObject(str, sim_total);
		list.add(so);
	}

	// train Word2Vec model
	public static void trainingW2v() throws IOException, SQLException {
		String exe = "python";
		String command = "training.py";
		String corups = "addresscorpus_D1.txt";
		String model = "addressmodel_D1.model";
		String word_vector = "addressvector_D1.vector";
		String[] cmdArr = new String[] { exe, command, corups, model, word_vector };
		Process process = Runtime.getRuntime().exec(cmdArr);
		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	// address matching
	// 1 Divide the input address into an address list by space, and extract the
	// country, city, street, and detailed address information.
	// 2 Clear the list for storing the best match results.
	// 3 For the country information, read all the country information from the
	// database, calculate the similarity between the input country and the country
	// information in the database, and add the result to the best matching list of
	// countries.
	// 4 For city information, read all city information from the database,
	// calculate the similarity between the input city and the city information in
	// the database, and add the result to the city best matching list.
	// 5 For the street information, read all the street information from the
	// database, calculate the similarity between the input street and the street
	// information in the database, and add the result to the street best matching
	// list.
	// 6 Iterate through the country Best matching list, city best matching list,
	// and street best matching list, construct the search criteria for matching
	// addresses, query the matched address information, and add the matching
	// results to the house number best matching list.
	// 7 If there is a matching result in the best matching list, the result is
	// written to the similaritymatch_D1.txt file. Otherwise, the matching address
	// is queried again using the country and street information and the result is
	// written to the file "similaritymatch_D1.txt".
	static void AddressMatch(String address, Connection conn, Map<String, String> abb_map, IDictionary dict)
			throws SQLException, IOException {
		String[] addresslist = address.split(" ");
		String poi_country = addresslist[3];
		String poi_city = addresslist[2];
		String poi_street = addresslist[1];
		String poi_address = addresslist[0] + "_" + addresslist[1] + "_" + addresslist[2] + "_" + addresslist[3];

		String[] tar_address = poi_address.split("_");
		String[] tar_country = poi_country.split("_");
		String[] tar_city = poi_city.split("_");
		String[] tar_street = poi_street.split("_");

		country_bestlist.clear();
		String sql = "select  *  from COUNTRY";
		pstm = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		System.out.println("a");
		rs = OracleConnection.SelectData(pstm);
		while (rs.next()) {
			String country = rs.getString("COUNTRY");
			String[] src_country = country.split("_");
			SimilarityMatch(poi_country, country, tar_country, src_country, abb_map, dict, country_bestlist, country,
					1.0 / 3, 1.0 / 3, 1.0 / 3);
		}
		ArrayList<SimilarObject> bestcountrys = selectMax(country_bestlist);

		city_bestlist.clear();
		System.out.println("b");
		sql = "select  *  from CITY";
		pstm = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rs = OracleConnection.SelectData(pstm);
		while (rs.next()) {
			String city = rs.getString("CITY");
			String[] src_city = city.split("_");
			SimilarityMatch(poi_city, city, tar_city, src_city, abb_map, dict, city_bestlist, city, 1.0 / 3, 1.0 / 3,
					1.0 / 3);
		}
		ArrayList<SimilarObject> bestcitys = selectMax(city_bestlist);

		street_bestlist.clear();
		System.out.println("c");
		sql = "select  *  from STREET";
		pstm = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rs = OracleConnection.SelectData(pstm);
		while (rs.next()) {
			{
				String street = rs.getString("STREET");
				String[] src_street = street.split("_");
				SimilarityMatch(poi_street, street, tar_street, src_street, abb_map, dict, street_bestlist, street,
						1.0 / 3, 1.0 / 3, 1.0 / 3);
			}
		}
		ArrayList<SimilarObject> beststreets = selectMax(street_bestlist);

		doorplate_bestlist.clear();

		System.out.println("d");

		ArrayList<String> poi_list = FileOperator.readURI("POIs_D1.txt");
		for (int m_k = 0; m_k < bestcountrys.size(); m_k++) {
			for (int m_i = 0; m_i < bestcitys.size(); m_i++) {
				for (int m_j = 0; m_j < beststreets.size(); m_j++) {
					// sql = " SELECT y FROM TABLE(SEM_MATCH( '{"
					// + "?y <http://www.poi.org/poi#hasCountry> <http://www.poi.org/poi#country/"
					// + bestcountrys.get(m_k).name
					// + ">. ?y <http://www.poi.org/poi#hasCity> <http://www.poi.org/poi#city/"
					// + bestcitys.get(m_i).name
					// + ">. ?y <http://www.poi.org/poi#hasStreet> <http://www.poi.org/poi#street/"
					// + beststreets.get(m_j).name
					// + ">}',"
					// + "SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')),
					// null))";
					// System.out.println(sql);
					// pstm = conn.prepareStatement(sql);
					// rs = pstm.executeQuery();
					List<String> matchedAddresses = new ArrayList<>();
					for (String address_String : poi_list) {
						if (address_String.contains(bestcountrys.get(m_k).name)
								&& address_String.contains(bestcitys.get(m_i).name)
								&& address_String.contains(beststreets.get(m_j).name)) {
							matchedAddresses.add(address_String);
						}
					}
					List<String[]> matchedRows = new ArrayList<>();
					for (String addressString : matchedAddresses) {
						matchedRows.add(new String[] { addressString });
					}
					for (String[] row : matchedRows) {
						String address_uri = row[0];
						String[] address_uri_list = address_uri.split("/");
						String addressstr = address_uri_list[address_uri_list.length - 1];
						String[] addressstrlist = addressstr.split("_");
						SimilarityMatch(poi_address, addressstr, tar_address, addressstrlist, abb_map, dict,
								doorplate_bestlist, addressstr, 1.0 / 3, 1.0 / 3, 1.0 / 3);
					}
				}
			}
		}
		selectSort(doorplate_bestlist);
		System.out.println(doorplate_bestlist);

		if (doorplate_bestlist.size() > 0) {
			FileOperator.writeFile(poi_address, "similaritymatch_D1.txt");
			for (int j = 0; j < doorplate_bestlist.size(); j++) {
				String str = "Match: " + doorplate_bestlist.get(j).name + " " + doorplate_bestlist.get(j).similarity;
				FileOperator.writeFile(str, "similaritymatch_D1.txt");
			}
		} else {
			for (int m_k = 0; m_k < bestcountrys.size(); m_k++) {
				for (int m_j = 0; m_j < beststreets.size(); m_j++) {
					// sql = " SELECT y FROM TABLE(SEM_MATCH( '{"
					// + "?y <http://www.poi.org/poi#hasCountry> <http://www.poi.org/poi#country/"
					// + bestcountrys.get(m_k).name
					// + ">. ?y <http://www.poi.org/poi#hasStreet> <http://www.poi.org/poi#street/"
					// + beststreets.get(m_j).name
					// + ">}',"
					// + "SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')),
					// null))";
					// pstm = conn.prepareStatement(sql);
					// rs = pstm.executeQuery();
					// while (rs.next()) {
					// String address_uri = rs.getString(1);
					// String[] address_uri_list = address_uri.split("/");
					// String addressstr = address_uri_list[address_uri_list.length - 1];
					// String[] addressstrlist = addressstr.split("_");
					// SimilarityMatch(poi_address, addressstr, tar_address, addressstrlist,
					// abb_map, dict,
					// doorplate_bestlist, addressstr, 1.0 / 3, 1.0 / 3, 1.0 / 3);
					// }
					List<String> matchedAddresses = new ArrayList<>();
					for (String address_String : poi_list) {
						if (address_String.contains(bestcountrys.get(m_k).name)
								&& address_String.contains(beststreets.get(m_j).name)) {
							matchedAddresses.add(address_String);
						}
					}
					List<String[]> matchedRows = new ArrayList<>();
					for (String addressString : matchedAddresses) {
						matchedRows.add(new String[] { addressString });
					}
					for (String[] row : matchedRows) {
						String address_uri = row[0];
						String[] address_uri_list = address_uri.split("/");
						String addressstr = address_uri_list[address_uri_list.length - 1];
						String[] addressstrlist = addressstr.split("_");
						SimilarityMatch(poi_address, addressstr, tar_address, addressstrlist, abb_map, dict,
								doorplate_bestlist, addressstr, 1.0 / 3, 1.0 / 3, 1.0 / 3);
					}
				}
			}

			selectSort(doorplate_bestlist);
			System.out.println("f");
			FileOperator.writeFile(poi_address, "similaritymatch_D1.txt");
			for (int j = 0; j < doorplate_bestlist.size(); j++) {
				String str = "Match: " + doorplate_bestlist.get(j).name + " " + doorplate_bestlist.get(j).similarity;
				FileOperator.writeFile(str, "similaritymatch_D1.txt");
			}
		}
	}

	// find the best tile in the ELLG
	public static void findBestTile(int column0, int row0, double resolu, double dis, int column, int row, int level,
			ArrayList<ArrayList<Code>> multicode, String dir) throws IOException {
		if (level <= 27) {
			double column1 = column * Math.pow(2, 27 - level); // the left boudary of this tile
			double column2 = (column + 1) * Math.pow(2, 27 - level) - 1; // the right boudary of this tile
			double row1 = row * Math.pow(2, 27 - level); // the down boudary of this tile
			double row2 = (row + 1) * Math.pow(2, 27 - level) - 1; // the upper boudary of this tile
			double col_far = 0;
			double col_near = 0;
			double row_near = 0;
			double row_far = 0;
			if (dir.equals("NW")) {
				col_far = column1;
				col_near = column2;
				row_near = row1;
				row_far = row2;
			} else if (dir.equals("NE")) {
				col_far = column2;
				col_near = column1;
				row_near = row1;
				row_far = row2;
			} else if (dir.equals("SW")) {
				col_far = column1;
				col_near = column2;
				row_near = row2;
				row_far = row1;
			} else if (dir.equals("SE")) {
				col_far = column2;
				col_near = column1;
				row_near = row2;
				row_far = row1;
			}
//			System.out.println("column0: " + column0 + " column1: " + column1 + " column2: " + column2);
			// System.out.println("col_far: " + col_far + " col_near: " + col_near + " row_near: " + row_near + " row_far: "
			// 		+ row_far + " column: " + column + " row: " + row + " level: " + level);
			// System.out.println(col_far/column + " " + col_near/column + " " + row_near/row + " " + row_far/row);
			// Euclid distance
			double dis_min = Math
					.sqrt(Math.pow((col_near - column0) * resolu, 2) + Math.pow((row_near - row0) * resolu, 2));
			double dis_max = Math
					.sqrt(Math.pow((col_far - column0) * resolu, 2) + Math.pow((row_far - row0) * resolu, 2));
			System.out.println("dis_min: " + dis_min + " dis_max: " + dis_max + " dis: " + dis);
			HashSet<Code> ninelist = new HashSet<Code>();
			if (dis_min <= dis && dis <= dis_max) {
				Code c = new Code(column, row, level);
				// System.out.println("add c" + column + " " + row + " " + level);
				if (containPOI(c)) {
					multicode.get(level).add(c);
					// System.out.println("add c" + column + " " + row + " " + level);
					findBestTile(column0, row0, resolu, dis, column * 2, row * 2, level + 1, multicode, dir);
					findBestTile(column0, row0, resolu, dis, column * 2 + 1, row * 2, level + 1, multicode, dir);
					findBestTile(column0, row0, resolu, dis, column * 2, row * 2 + 1, level + 1, multicode, dir);
					findBestTile(column0, row0, resolu, dis, column * 2 + 1, row * 2 + 1, level + 1, multicode, dir);
				}
			}
		}
	}

	public static void findTrialBestTile(double longitude0, double lantitude0, double dis, int column, int row, int level,
			ArrayList<ArrayList<Code>> multicode, String dir) throws IOException {
		if (level <= 27) {
			double longitude1 = column * Math.pow(2, 27 - level); // the left boudary of this tile
			double longitude2 = (column + 1) * Math.pow(2, 27 - level) - 1; // the right boudary of this tile
			double lantitude1 = row * Math.pow(2, 27 - level); // the down boudary of this tile
			double lantitude2 = (row + 1) * Math.pow(2, 27 - level) - 1; // the upper boudary of this tile
			double longitude_far = 0;
			double longitude_near = 0;
			double lantitude_near = 0;
			double lantitude_far = 0;
			if (dir.equals("NW")) {
				longitude_far = longitude1;
				longitude_near = longitude2;
				lantitude_near = lantitude1;
				lantitude_far = lantitude2;
			} else if (dir.equals("NE")) {
				longitude_far = longitude2;
				longitude_near = longitude1;
				lantitude_near = lantitude1;
				lantitude_far = lantitude2;
			} else if (dir.equals("SW")) {
				longitude_far = longitude1;
				longitude_near = longitude2;
				lantitude_near = lantitude2;
				lantitude_far = lantitude1;
			} else if (dir.equals("SE")) {
				longitude_far = longitude2;
				longitude_near = longitude1;
				lantitude_near = lantitude2;
				lantitude_far = lantitude1;
			}
			// System.out.println("col_far: " + col_far + " col_near: " + col_near + " row_near: " + row_near + " row_far: "
			// 		+ row_far + " column: " + column + " row: " + row + " level: " + level);
			// System.out.println(col_far/column + " " + col_near/column + " " + row_near/row + " " + row_far/row);
			// Euclid distance
			double dis_min = Math
					.sqrt(Math.pow((longitude_near - longitude0) * 3.14 * 6371000 / Math.pow(2, 27), 2)
							+ Math.pow((lantitude_near - lantitude0) * 3.14 * 6371000 / Math.pow(2, 27), 2));
			double dis_max = Math.sqrt(Math.pow((longitude_far - longitude0) * 3.14 * 6371000 / Math.pow(2, 27), 2)
					+ Math.pow((lantitude_far - lantitude0) * 3.14 * 6371000 / Math.pow(2, 27), 2));
			// System.out.println("dis_min: " + dis_min + " dis_max: " + dis_max + " dis: " + dis);
			HashSet<Code> ninelist = new HashSet<Code>();
			if (dis_min <= dis && dis <= dis_max) {
				Code c = new Code(column, row, level);
				// System.out.println("c" + column + " " + row + " " + level);
				if (containPOI(c)) {
					multicode.get(level).add(c);
					System.out.println("add c" + column + " " + row + " " + level);
					findTrialBestTile(longitude0, lantitude0, dis, column * 2, row * 2, level + 1, multicode, dir);
					findTrialBestTile(longitude0, lantitude0, dis, column * 2 + 1, row * 2, level + 1, multicode, dir);
					findTrialBestTile(longitude0, lantitude0, dis, column * 2, row * 2 + 1, level + 1, multicode, dir);
					findTrialBestTile(longitude0, lantitude0, dis, column * 2 + 1, row * 2 + 1, level + 1, multicode, dir);
				}
			}
		}
	}

	// judge whether the code contains POI
	public static boolean containPOI(Code c) throws IOException {
		HashSet<Code> clist = initmulticode.get(c.level);
		if (clist.contains(c))
			return true;
		else
			return false;
	}

	// Based on the description information (including distance, direction, and
	// address) entered, the qualified POI (Point of Interest) is found in the
	// database.
	// 1 The method accepts two parameters: discription for description information
	// and conn for database connection.
	// 2 the distance, direction and address information are extracted by dividing
	// the description information according to the space.
	// 3 Initializes several variables, including targetpoi, a collection that
	// stores the target POI, and multicode, a two-dimensional list that stores
	// different levels of tile code.
	// 4 Calculate tile resolution resolu, and initialize the column number column0
	// and row number row0 of the current tile.
	// 5 Determine the specific location ("NE", "NW", "SE", "SW", "N", "S", "E",
	// "W") according to the direction, and query the corresponding tile code from
	// the database according to the location and address.
	// 6 If a matching tile code is found, the new target tile location is
	// calculated based on the direction and distance, and then the database is
	// queried again to obtain the POI information for that location and add it to
	// the target POI collection.
	// 7 Finally, the size of the target POI collection is returned as a result.
	public static int ReasonDistanceDirection(String discription, Connection conn) throws IOException, SQLException {
		String[] stringlist = discription.split(" ");
		double dis = Double.valueOf(stringlist[0]);
		String direction = stringlist[1];
		String address = stringlist[2];

		HashSet<String> targetpoi = new HashSet<String>(); // target POI fulfill requirement
		ArrayList<ArrayList<Code>> multicode = new ArrayList<ArrayList<Code>>(); // initialize 2 dimensional list to
																					// store
																					// different level of tile code
		for (int i = 0; i < 28; i++) {
			ArrayList<Code> clist = new ArrayList<Code>();
			multicode.add(clist);
		}
		double resolu = 3.14 * 6371000 / Math.pow(2, 27); // gridsize under scale Lmax, obtained from distance systems
		int column0 = 0;
		int row0 = 0;
		String dir = null;
		if (direction.equals("NE")) {
			dir = "NE";
		} else if (direction.equals("NW")) {
			dir = "NW";
		} else if (direction.equals("SE")) {
			dir = "NW";
		} else if (direction.equals("SW")) {
			dir = "NE";
		} else if (direction.equals("N")) {
			dir = "N";
		} else if (direction.equals("S")) {
			dir = "N";
		} else if (direction.equals("E")) {
			dir = "E";
		} else if (direction.equals("W")) {
			dir = "E";
		}
		// clear the multicode and targetpoi
		for (int i = 0; i < 28; i++) {
			multicode.get(i).clear();
		}
		targetpoi.clear();
		double ts = System.nanoTime();
		// original tile
		String sql = "SELECT  g1 FROM TABLE(SEM_MATCH( '{"
				+ "?x1 <http://www.poi.org/poi#hasAddress> <http://www.poi.org/poi#address/"
				+ address
				+ ">.?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x3 <http://www.poi.org/poi#hasCode> ?g1. "
				+ "?g1 <http://www.poi.org/poi#hasScale> \"27\""
				+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
		PreparedStatement pstm = conn.prepareStatement(sql);
		ResultSet rs = pstm.executeQuery();
		while (rs.next()) {
			String str = rs.getString(1); // tile code
			String[] strlist = str.split("/"); // split the tile code
			String[] list = strlist[strlist.length - 1].split("_"); // split the tile code
			column0 = Integer.valueOf(list[1]); // column number of the tile
			row0 = Integer.valueOf(list[2]); // row number of the tile
		}
		ArrayList<String> codelist = new ArrayList<String>();
		if (direction.equals("NE") || direction.equals("NW") || direction.equals("N") || direction.equals("E")) {
			sql = "SELECT  g2 FROM TABLE(SEM_MATCH( '{"
					+ "?x1 <http://www.poi.org/poi#hasAddress> <http://www.poi.org/poi#address/"
					+ address
					+ ">.?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x3 <http://www.poi.org/poi#hasCode> ?g1. "
					+ "?g2 <http://www.poi.org/poi#"
					+ dir
					+ "> ?g1."
					+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
		} else {
			sql = "SELECT  g2 FROM TABLE(SEM_MATCH( '{"
					+ "?x1 <http://www.poi.org/poi#hasAddress> <http://www.poi.org/poi#address/"
					+ address
					+ ">.?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x3 <http://www.poi.org/poi#hasCode> ?g1. "
					+ "?g1 <http://www.poi.org/poi#"
					+ dir
					+ "> ?g2."
					+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
		}
		pstm = conn.prepareStatement(sql);
		rs = pstm.executeQuery();
		while (rs.next()) {
			String code = rs.getString(1);
			codelist.add(code);
		}
		if (codelist.size() > 0) {
			if (direction.equals("N") || direction.equals("E") || direction.equals("S") || direction.equals("W")) {
				int delta = (int) (dis / resolu) + 1;
				int deltamin = Integer.MAX_VALUE;
				Code ct = new Code();
				if (direction.equals("N")) {
					for (String str : codelist) {
						String[] str_code = str.split("/");
						String[] str_list = str_code[str_code.length - 1].split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (row == row0 + delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(row - (row0 + delta)) < deltamin) {
								deltamin = Math.abs(row - (row0 + delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				} else if (direction.equals("E")) {
					for (String str : codelist) {
						String[] str_code = str.split("/");
						String[] str_list = str_code[str_code.length - 1].split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (column == column0 + delta) { // the result fulfills the distance requirement 
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else { // the result does not fulfill the distance requirement
							if (Math.abs(column - (column0 + delta)) < deltamin) { // find the nearest result
								deltamin = Math.abs(column - (column0 + delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				} else if (direction.equals("S")) {
					for (String str : codelist) {
						String[] str_code = str.split("/");
						String[] str_list = str_code[str_code.length - 1].split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (row == row0 - delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(row - (row0 - delta)) < deltamin) {
								deltamin = Math.abs(row - (row0 - delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				} else if (direction.equals("W")) {
					for (String str : codelist) {
						String[] str_code = str.split("/");
						String[] str_list = str_code[str_code.length - 1].split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (column == column0 - delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(column - (column0 - delta)) < deltamin) {
								deltamin = Math.abs(column - (column0 - delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				}
				// have found near tiles, need to check if target POI exists
				sql = "SELECT x FROM TABLE(SEM_MATCH( '{"
						+ "?x3 <http://www.poi.org/poi#hasCode> <http://www.poi.org/poi#code/"
						+ ct.level + "_" + ct.x + "_" + ct.y
						+ ">. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x1 <http://www.poi.org/poi#hasAddress> ?x"
						+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
				pstm = conn.prepareStatement(sql);
				rs = pstm.executeQuery();
				while (rs.next()) {
					String poi = rs.getString(1);
					targetpoi.add(poi);
				}
				targetpois = targetpoi.size();
				String time = String.valueOf((System.nanoTime() - ts) * 0.000001);
				FileOperator.writeFile("discription: " + dis + " " + direction + " " + address,
						"reasoningresult_D1.txt");
				FileOperator.writeFile("proposed: " + time, "reasontime_D1.txt");
				for (String str : targetpoi) {
					String[] strlist = str.split("/");
					String a = strlist[strlist.length - 1];
					FileOperator.writeFile("target: " + a, "reasoningresult_D1.txt");
				}
			} // SNEW
			else {
				for (String str : codelist) {
					String[] str_code = str.split("/");
					String[] str_list = str_code[str_code.length - 1].split("_");
					int level = Integer.valueOf(str_list[0]);
					int column = Integer.valueOf(str_list[1]);
					int row = Integer.valueOf(str_list[2]);
					findBestTile(column0, row0, resolu, dis, column, row, level, multicode, direction);
				}
				for (int i = 27; i >= 0; i--) {
					if (multicode.get(i).size() > 0) {
						for (Code c : multicode.get(i)) {
							sql = "SELECT x FROM TABLE(SEM_MATCH( '{"
									+ "?x3 <http://www.poi.org/poi#hasCode> <http://www.poi.org/poi#code/"
									+ c.level + "_" + c.x + "_" + c.y
									+ ">. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x1 <http://www.poi.org/poi#hasAddress> ?x"
									+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
							pstm = conn.prepareStatement(sql);
							rs = pstm.executeQuery();
							while (rs.next()) {
								String poi = rs.getString(1);
								targetpoi.add(poi);
							}
						}
						break;
					}
				}
				if (targetpoi.size() > 0) {
					targetpois = targetpoi.size();
					String time = String.valueOf((System.nanoTime() - ts) * 0.000001);
					FileOperator.writeFile("discription: " + dis + " " + direction + " " + address,
							"reasoningresult_D1.txt");
					FileOperator.writeFile("proposed: " + time, "reasontime_D1.txt");
					for (String str : targetpoi) {
						String[] strlist = str.split("/");
						String a = strlist[strlist.length - 1];
						FileOperator.writeFile("target: " + a, "reasoningresult_D1.txt");
					}
				}
			}
		}
		return targetpois;
	}

	public static int ReasonTrialDistanceDirection(String discription, Connection conn)
			throws IOException, SQLException {
		String[] stringlist = discription.split(" ");
		double dis = Double.valueOf(stringlist[0]);
		String direction = stringlist[1];
		String address = stringlist[2];
		Code c;
		String s = null;
		ArrayList<String> slist = new ArrayList<String>();

		HashSet<String> targetpoi = new HashSet<String>(); // target POI fulfill requirement
		ArrayList<ArrayList<Code>> multicode = new ArrayList<ArrayList<Code>>(); // initialize 2 dimensional list to
																					// store
																					// different level of tile code
		for (int i = 0; i < 28; i++) {
			ArrayList<Code> clist = new ArrayList<Code>();
			multicode.add(clist);
		}
		double resolu = 3.14 * 6371000 / Math.pow(2, 27); // perimeter of earth
		int column0 = 0;
		int row0 = 0;
		double longitude0 = 0;
		double latitude0 = 0;
		String dir = null;
		if (direction.equals("NE")) {
			dir = "NE";
		} else if (direction.equals("NW")) {
			dir = "NW";
		} else if (direction.equals("SE")) {
			dir = "NW";
		} else if (direction.equals("SW")) {
			dir = "NE";
		} else if (direction.equals("N")) {
			dir = "N";
		} else if (direction.equals("S")) {
			dir = "N";
		} else if (direction.equals("E")) {
			dir = "E";
		} else if (direction.equals("W")) {
			dir = "E";
		}
		// clear the multicode and targetpoi
		for (int i = 0; i < 28; i++) {
			multicode.get(i).clear();
		}
		targetpoi.clear();
		double ts = System.nanoTime();
		ArrayList<String> all_str_list = FileOperator.readURI("POIs_D1.txt");

		ArrayList<PoiAddress> poi_address_list = new ArrayList<PoiAddress>();
		for (int i = 0; i < all_str_list.size(); i++) {
			String[] strlist = all_str_list.get(i).split(" ");
			Double poiaddress_longitude = Double.valueOf(strlist[0]);
			Double poiaddress_latitude = Double.valueOf(strlist[1]);
			String poiaddress_address = strlist[2];
			String poiaddress_country = strlist[3];
			String poiaddress_city = strlist[4];
			String poiaddress_street = strlist[5];
			int poiaddress_level = 27;
			int poiaddress_column = (int) Math
					.floor(Math.pow(2, poiaddress_level) * (poiaddress_longitude + 180) / 360);
			int poiaddress_row = (int) Math.floor(Math.pow(2, poiaddress_level - 1) * (poiaddress_latitude + 90) / 180);
			PoiAddress a = new PoiAddress(i + 1, poiaddress_longitude, poiaddress_latitude, poiaddress_address,
					poiaddress_country, poiaddress_city, poiaddress_street, poiaddress_column, poiaddress_row,
					poiaddress_level);
			poi_address_list.add(a);
		}
		// find proper address
		for (int i = 0; i < poi_address_list.size(); i++) {
			PoiAddress a = poi_address_list.get(i);
			if (a.getPoiAddress_Address().equals(address)) {
				column0 = a.getPoiAddress_Column();
				row0 = a.getPoiAddress_Row();
				longitude0 = a.getPoiAddress_Longitude();
				latitude0 = a.getPoiAddress_Latitude();
			}
		}
		for (int i = 5; i<28; i++) {
			c = getC(longitude0, latitude0, i);
			s = c.level + "_" + c.x + "_" + c.y;
			slist.add(s);
		}
		ArrayList<String> codelist = new ArrayList<String>();
		if (direction.equals("NE") || direction.equals("NW") || direction.equals("N") || direction.equals("E")) {
			for (int j = 5; j < 28; j++) {
				String pathsString = j + "_dirc_D1.txt"; // neighbor tile code
				ArrayList<String> dir_list = FileOperator.readURI(pathsString);
				c = getC(longitude0, latitude0, j);
				s = c.level + "_" + c.x + "_" + c.y;
				for (int i = 0; i < dir_list.size(); i++) {
					String[] strlist = dir_list.get(i).split(" ");
					String strlist1 = strlist[0];
					String strlist2 = strlist[2];
					String strlist3 = strlist[1];
					if (strlist1.equals(s) && strlist3.equals(dir)) {
						codelist.add(strlist2);
					}
				}
			}
		} else {
			for (int j = 5; j < 28; j++) {
				String pathsString = j + "_dirc_D1.txt";
				ArrayList<String> dir_list = FileOperator.readURI(pathsString);
				c = getC(longitude0, latitude0, j);
				s = c.level + "_" + c.x + "_" + c.y;
				for (int i = 0; i < dir_list.size(); i++) {
					String[] strlist = dir_list.get(i).split(" ");
					String strlist1 = strlist[0];
					String strlist2 = strlist[2];
					String strlist3 = strlist[1];
					if (strlist2.equals(s) && strlist3.equals(dir)) {
						codelist.add(strlist1);
					}
				}
			}
		}
		System.out.println(codelist);
		// System.out.println(codelist);
		if (codelist.size() > 0) {
			if (direction.equals("N") || direction.equals("E") || direction.equals("S") || direction.equals("W")) {
				int delta = (int) (dis / resolu) + 1;
				int deltamin = Integer.MAX_VALUE;
				Code ct = new Code();
				if (direction.equals("N")) {
					for (String str : codelist) {
						String[] str_list = str.split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (row == row0 + delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(row - (row0 + delta)) < deltamin) {
								deltamin = Math.abs(row - (row0 + delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				} else if (direction.equals("E")) {
					for (String str : codelist) {
						String[] str_list = str.split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (column == column0 + delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(column - (column0 + delta)) < deltamin) {
								deltamin = Math.abs(column - (column0 + delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				} else if (direction.equals("S")) {
					for (String str : codelist) {
						String[] str_list = str.split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (row == row0 - delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(row - (row0 - delta)) < deltamin) {
								deltamin = Math.abs(row - (row0 - delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				} else if (direction.equals("W")) {
					for (String str : codelist) {
						String[] str_list = str.split("_");
						int level = Integer.valueOf(str_list[0]);
						int column = Integer.valueOf(str_list[1]);
						int row = Integer.valueOf(str_list[2]);
						if (column == column0 - delta) {
							ct.x = column;
							ct.y = row;
							ct.level = level;
						} else {
							if (Math.abs(column - (column0 - delta)) < deltamin) {
								deltamin = Math.abs(column - (column0 - delta));
								ct.x = column;
								ct.y = row;
								ct.level = level;
							}
						}
					}
				}
				// this place 2024.3.18
				// sql = "SELECT x FROM TABLE(SEM_MATCH( '{"
				// 		+ "?x3 <http://www.poi.org/poi#hasCode> <http://www.poi.org/poi#code/"
				// 		+ ct.level + "_" + ct.x + "_" + ct.y
				// 		+ ">. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x1 <http://www.poi.org/poi#hasAddress> ?x"
				// 		+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
				// pstm = conn.prepareStatement(sql);
				// rs = pstm.executeQuery();
				// while (rs.next()) {
				// 	String poi = rs.getString(1);
				// 	targetpoi.add(poi);
				// }
				// compute latitude and longitude of ct
				for (int i = 0; i < all_str_list.size(); i++) {
					String[] strlist = all_str_list.get(i).split(" ");
					String ct_address = strlist[2];
					Double ct_longitude = Double.valueOf(strlist[0]);
					Double ct_latitude = Double.valueOf(strlist[1]);
					int ct_level = 27;
					int ct_column = (int) Math
							.floor(Math.pow(2, ct_level) * (ct_longitude + 180) / 360);
					int ct_row = (int) Math.floor(Math.pow(2, ct_level - 1) * (ct_latitude + 90) / 180);
					if (ct_column == ct.x && ct_row == ct.y && ct_level == ct.level) {
						targetpoi.add(ct_address);
					}
				}
				targetpois = targetpoi.size();
				String time = String.valueOf((System.nanoTime() - ts) * 0.000001);
				FileOperator.writeFile("discription: " + dis + " " + direction + " " + address,
						"reasoningresult_D1.txt");
				FileOperator.writeFile("proposed: " + time, "reasontime_D1.txt");
				for (String str : targetpoi) {
					FileOperator.writeFile("target: " + str, "reasoningresult_D1.txt");
				}
			}

			else { // double orientation
				for (String str : codelist) {
					String[] str_list = str.split("_");
					int level = Integer.valueOf(str_list[0]);
					int column = Integer.valueOf(str_list[1]);
					int row = Integer.valueOf(str_list[2]);
					System.out.println(column + " " + row + " " + level + column0 + " " + row0 + " " + resolu);
					findBestTile(column0, row0, resolu, dis, column, row, level, multicode, direction);
				}
				for (int i = 27; i >= 0; i--) {
					if (multicode.get(i).size() > 0) {
						// for (Code c : multicode.get(i)) {
						// 	sql = "SELECT x FROM TABLE(SEM_MATCH( '{"
						// 			+ "?x3 <http://www.poi.org/poi#hasCode> <http://www.poi.org/poi#code/"
						// 			+ c.level + "_" + c.x + "_" + c.y
						// 			+ ">. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x1 <http://www.poi.org/poi#hasAddress> ?x"
						// 			+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
						// 	pstm = conn.prepareStatement(sql);
						// 	rs = pstm.executeQuery();
						// 	while (rs.next()) {
						// 		String poi = rs.getString(1);
						// 		targetpoi.add(poi);
						// 	}
						// }
						// break;
						for (Code temp_c : multicode.get(i)) {// check
							for (int j = 0; j < all_str_list.size(); j++) {
								String[] strlist = all_str_list.get(j).split(" ");
								String ct_address = strlist[2];
								Double ct_longitude = Double.valueOf(strlist[0]);
								Double ct_latitude = Double.valueOf(strlist[1]);
								int ct_level = i;
								int ct_column = (int) Math
										.floor(Math.pow(2, ct_level) * (ct_longitude + 180) / 360);
								int ct_row = (int) Math.floor(Math.pow(2, ct_level - 1) * (ct_latitude + 90) / 180);
								if (ct_column == temp_c.x && ct_row == temp_c.y && ct_level == temp_c.level) {
									targetpoi.add(ct_address);
								}
							}
						}
					}
				}
				if (targetpoi.size() > 0) {
					targetpois = targetpoi.size();
					String time = String.valueOf((System.nanoTime() - ts) * 0.000001);
					FileOperator.writeFile("discription: " + dis + " " + direction + " " + address,
							"reasoningresult_D1.txt");
					FileOperator.writeFile("proposed: " + time, "reasontime_D1.txt");
					for (String str : targetpoi) {
						String[] strlist = str.split("/");
						String a = strlist[strlist.length - 1];
						FileOperator.writeFile("target: " + a, "reasoningresult_D1.txt");
					}
				}
			}
		}
		System.out.println(targetpois);
		return targetpois;
		// return 0;
	}

	// Based on the description information entered, including distance, direction,
	// and address, a POI similar to the target location is found in the database
	public static void ReasonGeneral(String discription, Connection conn, int num) throws IOException, SQLException {
		String[] stringlist = discription.split(" ");
		double dis = Double.valueOf(stringlist[0]);
		String direction = stringlist[1];
		String address = stringlist[2];

		ArrayList<SimilarObject> similarobjectlist = new ArrayList<SimilarObject>();
		double match_latitude = 0;
		double match_longitude = 0;
		double threshold = 0.15 * 180 / (6371000 * 3.14);
		double tss = System.nanoTime();
		String sql = "SELECT  x,y FROM TABLE(SEM_MATCH( '{"
				+ "?x1 <http://www.poi.org/poi#hasAddress> <http://www.poi.org/poi#address/"
				+ address
				+ ">.?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. ?x3 <http://www.poi.org/poi#hasRef> ?x4. "
				+ "?x4 <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?y. ?x4 <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?x."
				+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
		PreparedStatement pstm = conn.prepareStatement(sql);
		ResultSet rs = pstm.executeQuery();
		while (rs.next()) {
			match_latitude = Double.valueOf(rs.getString(2));
			match_longitude = Double.valueOf(rs.getString(1));
		}
		sql = "SELECT distinct  a,x,y FROM TABLE(SEM_MATCH( '{"
				+ "?x1 <http://www.poi.org/poi#hasAddress> ?a.?x2 <http://www.poi.org/poi#hasSemantic> ?x1. ?x2 <http://www.poi.org/poi#hasSpatial> ?x3. "
				+ "?x3 <http://www.poi.org/poi#hasRef> ?x4.?x4 <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?y. ?x4 <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?x. "
				+ "}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))";
		pstm = conn.prepareStatement(sql);
		rs = pstm.executeQuery();
		while (rs.next()) {
			String[] strlist = rs.getString(1).split("/");
			String addressstr = strlist[strlist.length - 1];
			double lat = Double.valueOf(rs.getString(3));
			double lon = Double.valueOf(rs.getString(2));
			if (direction.equals("NW")) {
				if (lat > match_latitude && lon < match_longitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("NE")) {
				if (lat > match_latitude && lon > match_longitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("SE")) {
				if (lat < match_latitude && lon > match_longitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("SW")) {
				if (lat < match_latitude && lon < match_longitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("N")) {
				if (Math.abs(lon - match_longitude) <= threshold && lat > match_latitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("E")) {
				if (Math.abs(lat - match_latitude) <= threshold && lon > match_longitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("S")) {
				if (Math.abs(lon - match_longitude) <= threshold && lat < match_latitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			} else if (direction.equals("W")) {
				if (Math.abs(lat - match_latitude) <= threshold && lon < match_longitude) {
					double poi_dis = Math.sqrt(Math.pow(Math.abs(lat - match_latitude) * 3.14 * 6371000 / 180, 2)
							+ Math.pow(Math.abs(lon - match_longitude) * 3.14 * 6371000 / 180, 2));
					double relative_dis = Math.abs(poi_dis - dis);
					SimilarObject s = new SimilarObject(addressstr, relative_dis);
					similarobjectlist.add(s);
				}
			}
			// }
		}
		selectSort(similarobjectlist);
		if (similarobjectlist.size() > 0 && num > 0) {
			String time = String.valueOf((System.nanoTime() - tss) * 0.000001);
			FileOperator.writeFile("general: " + time, "reasontime_D1.txt");
			for (int i = 0; i < Math.min(similarobjectlist.size(), num); i++) {
				String str = "truth: " + similarobjectlist.get(similarobjectlist.size() - 1 - i).name;
				FileOperator.writeFile(str, "reasoningresult_D1.txt");
			}
		}
	}

	// initialization
	public static void init() throws IOException, SQLException {
		conn = OracleConnection.getConnection();
		dict = InitWordNet();
		WuAndPalmer.WuAndPalmer(dict);

		String sql = "select  *  from ABB";
		pstm = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		// System.out.println(pstm);
		rs = OracleConnection.SelectData(pstm);
		while (rs.next()) {
			abb_map.put(rs.getString("ABBNAME"), rs.getString("FULLNAME"));
		}
		for (int i = 0; i < 28; i++) {
			HashSet<Code> clist = FileOperator.getcodelist(i);
			initmulticode.add(clist);
		}
		// System.out.println(initmulticode.get(27));
		// System.out.println(abb_map.size());
	}

	public static void main(String[] args) throws IOException, SQLException {

		init();
		System.out.println("1");
		ArrayList<String> referencelist =
		FileOperator.readURI("matchedaddress_D1.txt");
		System.out.println("2");
		for (int i = 0; i < referencelist.size(); i++) {
		System.out.println(referencelist.get(i));
		AddressMatch(referencelist.get(i), conn, abb_map, dict);
		}
		System.out.println("3");
		ArrayList<String> list = FileOperator.readURI("descriptions_D1.txt");
		System.out.println("4");
		for (String str : list) {
		int num = ReasonDistanceDirection(str, conn);
		}
	}
}
