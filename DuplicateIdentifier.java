//This is incomplete and not refactored!!!!
//Will fix it by end of Jan 2016
//Date created 27-01-2016

package InterviewCake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public class DuplicateIdentifier {

	ArrayList<Path> filePaths;
	HashMapList<LocalFile, Path> mappedFiles;
	static HashMapList<Long,LocalFile> filesBySize = new HashMapList<Long,LocalFile>();
	DuplicateIdentifier(){
		filePaths = new ArrayList<Path>();
		mappedFiles = new HashMapList<LocalFile, Path>();
	}
	class Path {
		String path;

		Path(String path) {
			this.path = path;
		}
	}

	class LocalFile implements Comparable{
		long size;
		String hash;
		byte[] digest;
		Path path;

		LocalFile(Path path) {
			calculateSize(path);
			this.path = path;
			filesBySize.put(size,this);
		}
		private void calculateSize(Path path){
			File file =new File(path.path);
			
			if(file.exists()){
				size = file.length();
			}else{
				size = -1;
			}
		}
		
		//This is not calculated by default, but only when there is another file with exactly the same size
		private void computeHash() {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(Files.readAllBytes(Paths.get(path.path)));
				digest = md.digest();
				this.hash = DatatypeConverter.printHexBinary(digest)
						.toUpperCase();
			} catch (NoSuchAlgorithmException | IOException e) {
				digest = null;
				this.hash = "(NULL): IO EXCEPTION";
				System.out.println("COULD NOT READ FILE: "+path.path);
			}
		}

		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if (other instanceof LocalFile) {
				LocalFile that = (LocalFile) other;
				result = (this.hash.equals(that.hash));
			}
			return result;
		}

		@Override
		public int hashCode() {
			return hash.hashCode();
		}
		boolean compareTo(LocalFile lf){
			return this.size<lf.size;
		}
		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	public void addFilePathsFromDirectory(String directory) {
		System.out.println("ADDING PATHS");
		System.out.println("================");
		walk(directory);
		System.out.println(filePaths.size()+" files were added");
	}
	
	public void walk(String directory) {
		File root = new File( directory );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	walk( f.getAbsolutePath() );
            }else{
            filePaths.add(new Path(f.getAbsoluteFile().getPath()));
            }
            }
	}

	private String beautifySize(Long size){
		String[] sizeNames = {"B","KB","MB","GB","TB"};
		int sizeNamesIndex=0;
		String inShort = size+" "+sizeNames[sizeNamesIndex];
		if (size<0) return "NEGATIVE";
		StringBuffer sb = new StringBuffer();
		final short DIVIDER = 10;
		final short GROUPSIZE = 3;
		short indexInGroup = 1;
		while(size>0){
			short digit = (short) (size%DIVIDER);
			size/=DIVIDER;
			sb.append(digit);
			if (indexInGroup==GROUPSIZE&&size>0){
				sb.append(".");
				indexInGroup=1;
				sizeNamesIndex++;
				inShort = size+" "+sizeNames[sizeNamesIndex];
			}else{
				indexInGroup++;
			}
		}
		return inShort+" ("+sb.reverse().toString()+" bytes)";
	}
	public void deleteUnique(){
		System.out.println("COMPUTING HASHES");
		System.out.println("================");
		for (Long filesize:filesBySize.keySet()){
			ArrayList<LocalFile> files = filesBySize.get(filesize);
			if (files.size()>1){
				for (LocalFile file : files) {
					file.computeHash();
					mappedFiles.put(file, file.path);
				}
			}
		}
	}

	public void createFiles() {
		System.out.println("CREATING FILES");
		System.out.println("================");
		for (Path path : filePaths) {
			new LocalFile(path);
		}
	}
	
	private void printList(ArrayList<Path> paths){
		int i=1;
		for (Path path:paths){
			System.out.print("\t"+i+": ");
			System.out.println(path.path);
			i++;
		}
	}

	public void printDuplicates(){
		System.out.println("PRINTING DUPLICATES");
		System.out.println("====================================");
		//PriorityQueue<LocalFile> sortedQueue = new PriorityQueue<LocalFile>
		
		List<LocalFile> filesBySize = new ArrayList<LocalFile>(mappedFiles.keySet());

	    Collections.sort(filesBySize, new Comparator<LocalFile>() {

	        public int compare(LocalFile f1, LocalFile f2) {
	            return (int) ((int) f2.size-f1.size);//possible overflow/underflow
	        }
	    });
	    
		for (LocalFile file:filesBySize){
			ArrayList<Path> paths = mappedFiles.get(file);
			if (paths.size()>1 && file.size>10000000L){
				
				System.out.println("FILE: MD5: "+file.hash+ " File size: "+beautifySize(file.size)+"\t");
				printList(paths);
				System.out.println();
			}
		}
	}

	public static void main(String[] args) {
		String directory = "some path here";
		DuplicateIdentifier di = new DuplicateIdentifier();
		di.addFilePathsFromDirectory(directory);
		di.createFiles();
		di.deleteUnique();
		di.printDuplicates();
	}
}
