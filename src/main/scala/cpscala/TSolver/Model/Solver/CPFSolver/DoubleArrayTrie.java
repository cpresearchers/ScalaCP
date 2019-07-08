package cpscala.TSolver.Model.Solver.CPFSolver;
//
//
////
////
/////*
////
////Author: zhenluhan
////Date:   6.6.2019
////
//// */


public class DoubleArrayTrie {



    public DoubleArrayTrie()
    {

    }

}














//
//import java.util.ArrayList;
//import java.util.Arrays;
//
//public class DoubleArrayTrie {
//    final int END_FLAG = -1;
//    final int DEFAULT_LEN = 1024;
//    int Base[]  = new int [DEFAULT_LEN];
//    int Check[] = new int [DEFAULT_LEN];
//    int Tail[] = new int [DEFAULT_LEN];
//    int Pos = 1;
//    int max_domain_size = 0;
//    //Map<Character ,Integer> CharMap = new HashMap<Character,Integer>();
//   // ArrayList<Character> CharList = new ArrayList<Character>();
//
//    public DoubleArrayTrie(int max)
//    {
//        Base[1] = 1;
//        max_domain_size = max;
//
//
//
//    }
//    private void Extend_Array()
//    {
//        Base = Arrays.copyOf(Base, Base.length*2);
//        Check = Arrays.copyOf(Check, Check.length*2);
//    }
//
//    private void Extend_Tail()
//    {
//        Tail = Arrays.copyOf(Tail, Tail.length*2);
//    }
//
//    private int GetCharCode(int c)
//    {
//        return c;
//    }
//    private int CopyToTailArray(int[] s,int p)
//    {
//        int _Pos = Pos;
//        while(s.length-p+1 > Tail.length-Pos)
//        {
//            Extend_Tail();
//        }
//        for(int i=p; i<s.length;++i)
//        {
//            Tail[_Pos] = s[i];
//            _Pos++;
//        }
//        return _Pos;
//    }
//
//    private int x_check(Integer []set)
//    {
//        for(int i=1; ; ++i)
//        {
//            boolean flag = true;
//            for(int j=0;j<set.length;++j)
//            {
//                int cur_p = i+set[j];
//                if(cur_p>= Base.length) Extend_Array();
//                if(Base[cur_p]!= 0 || Check[cur_p]!= 0)
//                {
//                    flag = false;
//                    break;
//                }
//            }
//            if (flag) return i;
//        }
//    }
//
//    private ArrayList<Integer> GetChildList(int p)
//    {
//        ArrayList<Integer> ret = new ArrayList<Integer>();
//        for(int i=1; i <= max_domain_size;++i)
//        {
//            if(Base[p]+i >= Check.length) break;
//            if(Check[Base[p]+i] == p)
//            {
//                ret.add(i);
//            }
//        }
//        return ret;
//    }
//
//    private boolean TailContainString(int start,int[] s2)
//    {
//        for(int i=0;i<s2.length;++i)
//        {
//            if(s2[i] != Tail[i+start]) return false;
//        }
//
//        return true;
//    }
//    private boolean TailMatchString(int start,int[] s2,int s)
//    {
//       //s2 += END_FLAG;
//        s2 = Arrays.copyOf(s2, s2.length+1);
//        s2[s2.length] = END_FLAG;
//        for(int i=s;i<s2.length;++i)
//        {
//            if(s2[i] != Tail[i+start])
//                return false;
//        }
//        return true;
//    }
//
//
//    public void Insert(int[] s) throws Exception
//    {
//
//        //s += END_FLAG;
//        s = Arrays.copyOf(s, s.length+1);
//        s[s.length] = END_FLAG;
//        int pre_p = 1;
//        int cur_p;
//        for(int i=0; i<s.length; ++i)
//        {
//            //获取状态位置
//            cur_p = Base[pre_p]+GetCharCode(s[i]);
//            //如果长度超过现有，拓展数组
//            if (cur_p >= Base.length) Extend_Array();
//
//            //空闲状态
//            if(Base[cur_p] == 0 && Check[cur_p] == 0)
//            {
//                Base[cur_p] = -Pos;
//                Check[cur_p] = pre_p;
//                Pos = CopyToTailArray(s,i+1);
//                break;
//            }else
//                //已存在状态
//                if(Base[cur_p] > 0 && Check[cur_p] == pre_p)
//                {
//                    pre_p = cur_p;
//                    continue;
//                }else
//                    //冲突 1：遇到 Base[cur_p]小于0的，即遇到一个被压缩存到Tail中的字符串
//                    if(Base[cur_p] < 0 && Check[cur_p] == pre_p)
//                    {
//                        int head = -Base[cur_p];
//
//                        if(s[i+1]== END_FLAG && Tail[head]== END_FLAG)	//插入重复字符串
//                        {
//                            break;
//                        }
//
//                        //公共字母的情况，因为上一个判断已经排除了结束符，所以一定是2个都不是结束符
//                        if (Tail[head] == s[i+1])
//                        {
//                            int avail_base = x_check(new Integer[]{GetCharCode(s[i+1])});
//                            Base[cur_p] = avail_base;
//
//                            Check[avail_base+GetCharCode(s[i+1])] = cur_p;
//                            Base[avail_base+GetCharCode(s[i+1])] = -(head+1);
//                            pre_p = cur_p;
//                            continue;
//                        }
//                        else
//                        {
//                            //2个字母不相同的情况，可能有一个为结束符
//                            int avail_base ;
//                            avail_base = x_check(new Integer[]{GetCharCode(s[i+1]),GetCharCode(Tail[head])});
//
//                            Base[cur_p] = avail_base;
//
//                            Check[avail_base+GetCharCode(Tail[head])] = cur_p;
//                            Check[avail_base+GetCharCode(s[i+1])] = cur_p;
//
//                            //Tail 为END_FLAG 的情况
//                            if(Tail[head] == END_FLAG)
//                                Base[avail_base+GetCharCode(Tail[head])] = 0;
//                            else
//                                Base[avail_base+GetCharCode(Tail[head])] = -(head+1);
//                            if(s[i+1] == END_FLAG)
//                                Base[avail_base+GetCharCode(s[i+1])] = 0;
//                            else
//                                Base[avail_base+GetCharCode(s[i+1])] = -Pos;
//
//                            Pos = CopyToTailArray(s,i+2);
//                            break;
//                        }
//                    }else
//                        //冲突2：当前结点已经被占用，需要调整pre的base
//                        if(Check[cur_p] != pre_p)
//                        {
//                            ArrayList<Integer> list1 = GetChildList(pre_p);
//                            int toBeAdjust;
//                            ArrayList<Integer> list = null;
//                            if(true)
//                            {
//                                toBeAdjust = pre_p;
//                                list = list1;
//                            }
//
//                            int origin_base = Base[toBeAdjust];
//                            list.add(GetCharCode(s[i+1]));
//                            int avail_base = x_check((Integer[])list.toArray(new Integer[list.size()]));
//                            list.remove(list.size()-1);
//
//                            Base[toBeAdjust] = avail_base;
//                            for(int j=0; j<list.size(); ++j)
//                            {
//                                //BUG
//                                int tmp1 = origin_base + list.get(j);
//                                int tmp2 = avail_base + list.get(j);
//
//                                Base[tmp2] = Base[tmp1];
//                                Check[tmp2] = Check[tmp1];
//
//                                //有后续
//                                if(Base[tmp1] > 0)
//                                {
//                                    ArrayList<Integer> subsequence = GetChildList(tmp1);
//                                    for(int k=0; k<subsequence.size(); ++k)
//                                    {
//                                        Check[Base[tmp1]+subsequence.get(k)] = tmp2;
//                                    }
//                                }
//
//                                Base[tmp1] = 0;
//                                Check[tmp1] = 0;
//                            }
//
//                            //更新新的cur_p
//                            cur_p = Base[pre_p]+GetCharCode(s[i+1]);
//
//                            if(s[i+1] == END_FLAG)
//                                Base[cur_p] = 0;
//                            else
//                                Base[cur_p] = -Pos;
//                            Check[cur_p] = pre_p;
//                            Pos = CopyToTailArray(s,i+1);
//                            break;
//                        }
//        }
//    }
//
//    public boolean Exists(int[] word)
//    {
//        int pre_p = 1;
//        int cur_p = 0;
//
//        for(int i=0;i<word.length;++i)
//        {
//            cur_p = Base[pre_p]+GetCharCode(word[i]);
//            if(Check[cur_p] != pre_p) return false;
//            if(Base[cur_p] < 0)
//            {
//                if(TailMatchString(-Base[cur_p],word,i+1))
//                    return true;
//                return false;
//            }
//            pre_p = cur_p;
//        }
//        if(Check[Base[cur_p]+GetCharCode(END_FLAG)] == cur_p)
//            return true;
//        return false;
//    }
//
//    //内部函数，返回匹配单词的最靠后的Base index，
//  /*  class FindStruct
//    {
//        int p;
//        String prefix="";
//    }
//    private FindStruct Find(String word)
//    {
//        int pre_p = 1;
//        int cur_p = 0;
//        FindStruct fs = new FindStruct();
//        for(int i=0;i<word.length();++i)
//        {
//            // BUG
//            fs.prefix += word.charAt(i);
//            cur_p = Base[pre_p]+GetCharCode(word.charAt(i));
//            if(Check[cur_p] != pre_p)
//            {
//                fs.p = -1;
//                return fs;
//            }
//            if(Base[cur_p] < 0)
//            {
//                if(TailContainString(-Base[cur_p],word.substring(i+1)))
//                {
//                    fs.p = cur_p;
//                    return fs;
//                }
//                fs.p = -1;
//                return fs;
//            }
//            pre_p = cur_p;
//        }
//        fs.p =  cur_p;
//        return fs;
//    }
//*/
//    public ArrayList<String> GetAllChildWord(int index)
//    {
//        ArrayList<String> result = new ArrayList<String>();
//        if(Base[index] == 0)
//        {
//            result.add("");
//            return result;
//        }
//        if(Base[index] < 0)
//        {
//            String r="";
//            for(int i = -Base[index]; Tail[i]!= END_FLAG; ++i)
//            {
//                r+= Tail[i];
//            }
//            result.add(r);
//            return result;
//        }
//        for(int i=1;i<=max_domain_size;++i)
//        {
//            if(Check[Base[index]+i] == index)
//            {
//                for(String s:GetAllChildWord(Base[index]+i))
//                {
//                    result.add(i+s);
//                }
//                //result.addAll(GetAllChildWord(Base[index]+i));
//            }
//        }
//        return result;
//    }
///*
//    public ArrayList<String> FindAllWords(String word)
//    {
//        ArrayList<String> result = new ArrayList<String>();
//        String prefix = "";
//        FindStruct fs = Find(word);
//        int p = fs.p;
//        if (p == -1) return result;
//        if(Base[p]<0)
//        {
//            String r="";
//            for(int i = -Base[p]; Tail[i]!= END_FLAG; ++i)
//            {
//                r+= Tail[i];
//            }
//            result.add(fs.prefix+r);
//            return result;
//        }
//
//        if(Base[p] > 0)
//        {
//            ArrayList<String> r =  GetAllChildWord(p);
//            for(int i=0;i<r.size();++i)
//            {
//                r.set(i, fs.prefix+r.get(i));
//            }
//            return r;
//        }
//
//        return result;
//    }
//*/
//}
//
//
//
////import java.util.ArrayList;
////import java.util.Arrays;
////
////
////public class DoubleArrayTrie {
////    final int END_FLAG = -1;
////    final int DEFAULT_LEN = 2048;
////    int Base[]  = new int [DEFAULT_LEN];
////    int Check[] = new int [DEFAULT_LEN];
////    int Tail[] = new int [DEFAULT_LEN];
////    int Pos = 1;
////    int max_domin = 0;
////   // Map<Character ,Integer> CharMap = new HashMap<Character,Integer>();
////   // ArrayList<Character> CharList = new ArrayList<Character>();
////
////    public DoubleArrayTrie(int max)
////    {
////        Base[1] = 1;
////        max_domin = max;
////
//////        CharMap.put(END_FLAG,1);
//////        CharList.add(END_FLAG);
//////        CharList.add(END_FLAG);
//////        for(int i=0;i<26;++i)
//////        {
//////            CharMap.put((char)('a'+i),CharMap.size()+1);
//////            CharList.add((char)('a'+i));
//////        }
////
////    }
////    private void Extend_Array()
////    {
////        Base = Arrays.copyOf(Base, Base.length*2);
////        Check = Arrays.copyOf(Check, Check.length*2);
////    }
////
////    private void Extend_Tail()
////    {
////        Tail = Arrays.copyOf(Tail, Tail.length*2);
////    }
////
//////    private int GetCharCode(char c)
//////    {
//////        if (!CharMap.containsKey(c))
//////        {
//////            CharMap.put(c,CharMap.size()+1);
//////            CharList.add(c);
//////        }
//////        return CharMap.get(c);
//////    }
////
////    private int CopyToTailArray(int[] s,int p)
////    {
////        int _Pos = Pos;
////        while(s.length-p+1 > Tail.length-Pos)
////        {
////            Extend_Tail();
////        }
////        for(int i=p; i<s.length;++i)
////        {
////            Tail[_Pos] = s[i];
////            _Pos++;
////        }
////        return _Pos;
////    }
////
////    private int x_check(Integer []set)
////    {
////        for(int i=1; ; ++i)
////        {
////            boolean flag = true;
////            for(int j=0;j<set.length;++j)
////            {
////                int cur_p = i+set[j];
////                if(cur_p>= Base.length)
////                    Extend_Array();
////                if(Base[cur_p]!= 0 || Check[cur_p]!= 0)
////                {
////                    flag = false;
////                    break;
////                }
////            }
////            if (flag)
////                return i;
////        }
////    }
////
////
////    private ArrayList<Integer> GetChildList(int p)
////    {
////        ArrayList<Integer> ret = new ArrayList<>();
////        for(int i=1; i<=max_domin;++i)
////        {
////            if(Base[p]+i >= Check.length)
////                break;
////            if(Check[Base[p]+i] == p)
////            {
////                ret.add(i);
////            }
////        }
////        return ret;
////    }
////
////
////    private boolean TailContainString(int start,int[] s2)
////    {
////        for(int i=0;i<s2.length;++i)
////        {
////            if(s2[i] != Tail[i+start])
////                return false;
////        }
////
////        return true;
////    }
////    private boolean TailMatchString(int start,int[] s2)
////    {
////       // s2 += END_FLAG;
////        for(int i=0;i<s2.length;++i)
////        {
////            if(s2[i] != Tail[i+start])
////                return false;
////        }
////        return true;
////    }
////
////
////    public void Insert(int[] s) throws Exception
////    {
////
////
////        int pre_p = 1;
////        int cur_p;
////        for(int i=0; i<s.length; ++i)
////        {
////
////            cur_p = Base[pre_p]+s[i];
////
////            if (cur_p >= Base.length)
////                Extend_Array();
////
////
////            if(Base[cur_p] == 0 && Check[cur_p] == 0)
////            {
////                Base[cur_p] = -Pos;
////                Check[cur_p] = pre_p;
////                Pos = CopyToTailArray(s,i+1);
////                break;
////            }else
////
////                if(Base[cur_p] > 0 && Check[cur_p] == pre_p)
////                {
////                    pre_p = cur_p;
////                    continue;
////                }else
////
////                    if(Base[cur_p] < 0 && Check[cur_p] == pre_p)
////                    {
////                        int head = -Base[cur_p];
////
////                        if(s[i+1]== END_FLAG && Tail[head]== END_FLAG)	//�����ظ��ַ���
////                        {
////                            break;
////                        }
////
////                        if (Tail[head] == s[i+1])
////                        {
////                            int avail_base = x_check(new Integer[]{s[i+1]});
////                            Base[cur_p] = avail_base;
////
////                            Check[avail_base+s[i+1]] = cur_p;
////                            Base[avail_base+s[i+1]] = -(head+1);
////                            pre_p = cur_p;
////                            continue;
////                        }
////                        else
////                        {
////
////                            int avail_base ;
////                            avail_base = x_check(new Integer[]{s[i+1],Tail[head]});
////
////                            Base[cur_p] = avail_base;
////
////                            Check[avail_base+Tail[head]] = cur_p;
////                            Check[avail_base+s[i+1]] = cur_p;
////
////                            //Tail ΪEND_FLAG �����
////                            if(Tail[head] == END_FLAG)
////                                Base[avail_base+Tail[head]] = 0;
////                            else
////                                Base[avail_base+Tail[head]] = -(head+1);
////                            if(s[i+1] == END_FLAG)
////                                Base[avail_base+s[i+1]] = 0;
////                            else
////                                Base[avail_base+s[i+1]] = -Pos;
////
////                            Pos = CopyToTailArray(s,i+2);
////                            break;
////                        }
////                    }else
////
////                        if(Check[cur_p] != pre_p)
////                        {
////                            ArrayList<Integer> list1 = GetChildList(pre_p);
////                            int toBeAdjust;
////                            ArrayList<Integer> list = null;
////                            if(true)
////                            {
////                                toBeAdjust = pre_p;
////                                list = list1;
////                            }
////
////                            int origin_base = Base[toBeAdjust];
////                            list.add(s[i]);
////                            int avail_base = x_check((Integer[])list.toArray(new Integer[list.size()]));
////                            list.remove(list.size()-1);
////
////                            Base[toBeAdjust] = avail_base;
////                            for(int j=0; j<list.size(); ++j)
////                            {
////                                //BUG
////                                int tmp1 = origin_base + list.get(j);
////                                int tmp2 = avail_base + list.get(j);
////
////                                Base[tmp2] = Base[tmp1];
////                                Check[tmp2] = Check[tmp1];
////
////                                //�к���
////                                if(Base[tmp1] > 0)
////                                {
////                                    ArrayList<Integer> subsequence = GetChildList(tmp1);
////                                    for(int k=0; k<subsequence.size(); ++k)
////                                    {
////                                        Check[Base[tmp1]+subsequence.get(k)] = tmp2;
////                                    }
////                                }
////
////                                Base[tmp1] = 0;
////                                Check[tmp1] = 0;
////                            }
////
////                            //�����µ�cur_p
////                            cur_p = Base[pre_p]+s[i];
////
////                            if(s[i]== END_FLAG)
////                                Base[cur_p] = 0;
////                            else
////                                Base[cur_p] = -Pos;
////                            Check[cur_p] = pre_p;
////                            Pos = CopyToTailArray(s,i+1);
////                            break;
////                        }
////        }
////    }
////
////    public boolean Exists(int[] word)
////    {
////        int pre_p = 1;
////        int cur_p = 0;
////
////        for(int i=0;i<word.length;++i)
////        {
////            cur_p = Base[pre_p]+word[i];
////            if(Check[cur_p] != pre_p) return false;
////            if(Base[cur_p] < 0)
////            {
////                if(TailMatchString(-Base[cur_p],word.substring(i+1)))
////                    return true;
////                return false;
////            }
////            pre_p = cur_p;
////        }
////        if(Check[Base[cur_p]+ -1] == cur_p)
////            return true;
////        return false;
////    }
////
////    /*
////    //�ڲ�����������ƥ�䵥�ʵ�����Base index��
////    class FindStruct
////    {
////        int p;
////        String prefix="";
////    }
////    private FindStruct Find(int[] word)
////    {
////        int pre_p = 1;
////        int cur_p = 0;
////        FindStruct fs = new FindStruct();
////        for(int i=0;i<word.length;++i)
////        {
////            // BUG
////            fs.prefix += word[i];
////            cur_p = Base[pre_p]+word[i];
////            if(Check[cur_p] != pre_p)
////            {
////                fs.p = -1;
////                return fs;
////            }
////            if(Base[cur_p] < 0)
////            {
////                if(TailContainString(-Base[cur_p],word.substring(i+1)))
////                {
////                    fs.p = cur_p;
////                    return fs;
////                }
////                fs.p = -1;
////                return fs;
////            }
////            pre_p = cur_p;
////        }
////        fs.p =  cur_p;
////        return fs;
////    }
////
////    public ArrayList<String> GetAllChildWord(int index)
////    {
////        ArrayList<String> result = new ArrayList<String>();
////        if(Base[index] == 0)
////        {
////            result.add("");
////            return result;
////        }
////        if(Base[index] < 0)
////        {
////            String r="";
////            for(int i = -Base[index]; Tail[i]!= END_FLAG; ++i)
////            {
////                r+= Tail[i];
////            }
////            result.add(r);
////            return result;
////        }
////        for(int i=1;i<=CharMap.size();++i)
////        {
////            if(Check[Base[index]+i] == index)
////            {
////                for(String s:GetAllChildWord(Base[index]+i))
////                {
////                    result.add(CharList.get(i)+s);
////                }
////                //result.addAll(GetAllChildWord(Base[index]+i));
////            }
////        }
////        return result;
////    }
////
////    public ArrayList<String> FindAllWords(String word)
////    {
////        ArrayList<String> result = new ArrayList<String>();
////        String prefix = "";
////        FindStruct fs = Find(word);
////        int p = fs.p;
////        if (p == -1) return result;
////        if(Base[p]<0)
////        {
////            String r="";
////            for(int i = -Base[p]; Tail[i]!= END_FLAG; ++i)
////            {
////                r+= Tail[i];
////            }
////            result.add(fs.prefix+r);
////            return result;
////        }
////
////        if(Base[p] > 0)
////        {
////            ArrayList<String> r =  GetAllChildWord(p);
////            for(int i=0;i<r.size();++i)
////            {
////                r.set(i, fs.prefix+r.get(i));
////            }
////            return r;
////        }
////
////        return result;
////    }
////    */
////
////}