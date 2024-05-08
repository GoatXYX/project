
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;


public class WuAndPalmer
{
  private static IDictionary dict = null;
  private static ArrayList<ISynsetID> roots = null;
  
  public static void getRoots(POS paramPOS)
  {
    ISynset localISynset = null;
    Iterator localIterator = null;
    List localList1 = null;
    List localList2 = null;
    localIterator = dict.getSynsetIterator(paramPOS);
    while (localIterator.hasNext())
    {
      localISynset = (ISynset)localIterator.next();
      localList1 = localISynset.getRelatedSynsets(Pointer.HYPERNYM);
      localList2 = localISynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);
      ISynsetID localid=localISynset.getID();
      roots.add(localid);
      break;
    }
  }

  public static void WuAndPalmer(IDictionary paramIDictionary)
  {
    dict = paramIDictionary;
    roots = new ArrayList();
    getRoots(POS.NOUN);
  }

  public static double wup(List<IWordID> localList1, int paramInt1, List<IWordID> localList2, int paramInt2, String paramString3)
  {
    double d1 = 0.0D;
    IWordID localIWordID1 = localList1.get(paramInt1 - 1);
    ISynset localISynset1 = dict.getWord(localIWordID1).getSynset();

    IWordID localIWordID2 = localList2.get(paramInt2 - 1);
    ISynset localISynset2 = dict.getWord(localIWordID2).getSynset();

    if (localISynset1.equals(localISynset2))
    {
      return 1.0D;
    }

    ArrayList<ArrayList<ISynsetID>> localArrayList1 = paths(localISynset1);
    ArrayList<ArrayList<ISynsetID>> localArrayList2 = paths(localISynset2);
    double d2 = 0.0D;

    for (ArrayList<ISynsetID> localArrayList3:localArrayList1) 
    {
      for (ArrayList<ISynsetID> localArrayList4 : localArrayList2)
      {
        double d3 = looking(localArrayList3, localArrayList4);
        if (d3 > d2)
        {
          d2 = d3;
        }
      }
    }
    d1 = d2;

    return d1;
  }

  private static double looking(ArrayList<ISynsetID> paramArrayList1, ArrayList<ISynsetID> paramArrayList2)
  {
    double d1 = paramArrayList1.size() + 1.0D;
    double d2 = paramArrayList2.size() + 1.0D;
    double d3 = 0.0D;
    double d4 = 0.0D;

    ArrayList<ISynsetID> localArrayList1 = new ArrayList<ISynsetID>();
    localArrayList1.addAll(paramArrayList1);
    localArrayList1.retainAll(paramArrayList2);

    if (localArrayList1.isEmpty())
    {
      d3 = 1.0D;
    }
    else
    {
      double d5 = 1.7976931348623157E+308D;
      ISynsetID localObject1 = null;
      for ( ISynsetID localISynsetID:localArrayList1)
      { 
        int i = paramArrayList1.indexOf(localISynsetID);
        int j = paramArrayList2.indexOf(localISynsetID);
        double d7 = i + j;
        if (d7 < d5)
        {
          d5 = d7;
          localObject1 = localISynsetID;
        }
      }

      if (localObject1.equals(paramArrayList1.get(0)))
      {
        d3 = d1;
      }
      else if (localObject1.equals(paramArrayList2.get(0)))
      {
        d3 = d2;
      }
      else
      {
        ArrayList<ArrayList<ISynsetID>> localObject2 = paths(dict.getSynset(localObject1));//localObject2存储最小公共节点到根节点的所有路径
        double d6 = 1.7976931348623157E+308D;
        for (ArrayList<ISynsetID> localArrayList2 : localObject2)
        {
          double d8 = localArrayList2.size();
          if (d8 < d6)
          {
            d6 = d8;
          }
        }
        d3 = d6 + 1.0D;
      }
    }

    d4 = 2.0D * d3 / (d1 + d2);

    if (d4 > 1.0D)
    {
      return 0.0D;
    }

    return d4;
  }

  private static ArrayList<ArrayList<ISynsetID>> paths(ISynset paramISynset)
  {
	  ArrayList<ArrayList<ISynsetID>> localArrayList1 = new ArrayList<ArrayList<ISynsetID>>();
    ArrayList<ISynsetID> localArrayList2 = new ArrayList<ISynsetID>();
    localArrayList2.add(paramISynset.getID());
    localArrayList1.add(localArrayList2);
    int i = 1;
    ArrayList<ArrayList<ISynsetID>> localArrayList4;
    while (i != 0)
    {
      for (int j = 0; j < localArrayList1.size(); j++)
      {
        i = 0;
        ArrayList<ISynsetID> localObject = localArrayList1.get(j);
        localArrayList4 = new ArrayList<ArrayList<ISynsetID>>();
        ISynsetID localISynsetID1 = localObject.get(localObject.size() - 1);
        HashSet<ISynsetID> localHashSet = hypernyms(localISynsetID1);
        if (!localHashSet.isEmpty())
        {
          i = 1;
          for (ISynsetID localISynsetID2 : localHashSet)
          {
            ArrayList<ISynsetID>  localArrayList5 = new ArrayList<ISynsetID>();
            localArrayList5.addAll(localObject);
            localArrayList5.add(localISynsetID2);
            localArrayList4.add(localArrayList5);
          }
        }
        localArrayList1.addAll(localArrayList4);
      }

    }

    ArrayList<ArrayList<ISynsetID>> localArrayList3 = new ArrayList<ArrayList<ISynsetID>>();

    for (ArrayList<ISynsetID> localArrayList6 :localArrayList1 ) 
    {
      if (roots.contains(localArrayList6.get(localArrayList6.size() - 1)))
      {
        localArrayList3.add(localArrayList6);
      }
    }
    return localArrayList3;
  }

  private static HashSet<ISynsetID> hypernyms(ISynsetID paramISynsetID)
  {
    HashSet<ISynsetID> localHashSet = new HashSet<ISynsetID>();
    ISynset localISynset = dict.getSynset(paramISynsetID);
    localHashSet.addAll(localISynset.getRelatedSynsets(Pointer.HYPERNYM));
    localHashSet.addAll(localISynset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE));
    return localHashSet;
  }

  public static  double wup(List<IWordID> localList1, List<IWordID> localList2, String paramString3)
  {
	  double d0=0;
	   for(int i=1;i<Math.min(localList1.size()+1, 3);i++)
      {
		 for(int j=1;j<Math.min(localList2.size()+1, 3);j++)
        {
			 double d = wup( localList1, i, localList2, j, paramString3);
	          if(d>d0)
	        	  d0=d;
        }
      }
    return d0;
  }
}
