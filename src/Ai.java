import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.mining.word2vec.DocVectorModel;
import com.hankcs.hanlp.mining.word2vec.WordVectorModel;

public class Ai {
	Map<String, ArrayList<String>> map;
	String robotName="小初";
	public Ai()
	{
		map=new HashMap<String,ArrayList<String>>();
		loadFile("word/"); 
	}	
	public void loadFile(String dirname) 
	{
        File f1 = new File(dirname);
        if (f1.isDirectory()) {
            String s[] = f1.list();
            for (int i = 0; i < s.length; i++) {
                File fn = new File(dirname + "/" + s[i]);
                if (fn.isFile()) {
                	//readFileToArrays(dirname+s[0]);
                } 
            }
            readFileToArrays("word/词库1.txt");
            readFileToArrays("word/词库2.txt");
            readFileToArrays("word/词库3.txt");
            readFileToArrays("word/词库4.txt");
            readFileToArrays("word/词库5.txt");
            readFileToArrays("word/综合词库.txt");
            readFileToArrays("word/骂人词库.txt");
        }
	}
	public void showWord()
	{
		for(Entry<String, ArrayList<String>> entry:map.entrySet())
		{
			
			System.out.println("key"+entry.getKey()+"value"+entry.getValue());
		}
	}
	public String ask(String str)
	{
		String answer=null;
		Random rd=new Random();
		String[] unknow={"对不起,我还听不懂你在说什么","再说一遍,行吗","现在我还听不懂呢","说明白简单一点呗"};
		if(map.get(str)!=null)
		{
			answer=map.get(str).get(rd.nextInt(map.get(str).size()));
		}
		else 
		{
			answer=unknow[rd.nextInt(unknow.length)];
		}
		return answer;
	}
	public String abstractAsk(String str)
	{
		str=str.replace("。"," ");
		str=str.replace("，"," ");
		str=str.trim();
		String answer=null;
		Random rd=new Random();
		String[] unknow={"对不起,我还听不懂你在说什么","再说一遍,行吗","现在我还听不懂呢","说明白简单一点呗"};
		for(Entry<String, ArrayList<String>> entry:map.entrySet())
		{
			if(WordCompare.getSimilarityRatio(str,entry.getKey())>0.5)
			{
				answer=entry.getValue().get(rd.nextInt(entry.getValue().size()));
				answer=answer.replace("%robotname%",robotName);
				return answer;
			}
			
		}
		answer=unknow[rd.nextInt(unknow.length)];
		return answer;
	}
	public void readFileToArrays(String path)
	{
		File f = new File(path);
        FileInputStream fip = null;
		try {
			fip = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 构建FileInputStream对象
 
        InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(fip, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 构建InputStreamReader对象,编码与写入相同
 
        BufferedReader br=new BufferedReader(reader);
        String line;
        try {
        	int step=0;
        	String question = null;
        	String answer = null;
			while ((line=br.readLine())!=null) {
				if(!line.equals(""
						+ ""))
				{
					if(step==0)
					{
						question=line;
						step=1;
					}
					else if(step==1)
					{
						answer=line;
						step=2;
					}
					else 
					{
						question=null;
						answer = null;
						step=0;
					}
				}
				else if(line.equals(""
						+ ""))
				{
					if(step==2)
					{
						if(map.get(question) != null&&question!=null&&answer != null)
						{
							map.get(question).add(answer);
							//System.out.println(question+","+map.get(question).get(0));
						}
						else if(map.get(question) == null&&question!=null&&answer != null)
						{
							ArrayList<String> list=new ArrayList<String>();
							list.add(answer);
							map.put(question,list);
							step=0;
							//System.out.println("问："+question+"答"+answer);
						}
					}
					else 
					{
						question=null;
						answer = null;
						step=0;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // 关闭读取流
 
        try {
			fip.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 	
	        // 关闭输入流,释放系统资源
		Ai ai=new Ai();	
		Scanner sc=new Scanner(System.in);
		System.out.println(ai.abstractAsk(sc.next()));
		//sc.close();*/
	}

	

}
