#coding=utf-8

import cx_Oracle      
import sys

from gensim.models import Word2Vec
from curses.ascii import isdigit

class FileOperator:
    def readURI(self,file):
        uris=[]
        fo = open(file,"r")
        lines=fo.readlines()
        for line in lines:
            uris.append(line)
        return uris
           
    def writeFile(self,str,path):
        fo = open(path, "a")
        content = str+"\n"
        fo.write(content)

class OracleConnection:
    def getConnection(self):
        conn=cx_Oracle.connect("system","123456" ,"ip:1521/ORCLL")
        cursor=conn.cursor()
        return cursor 
    
    def SelectData(self,cursor,sql):
        cursor.execute(sql)
        rs=cursor.fetchall()
        return rs

class TextSimilarity:
    def LevenshteinDistance(self, s, t):
        sLen = len(s) 
        tLen = len(t)
        if(sLen == 0):
            return tLen
        if(tLen == 0):
            return sLen
        
        d=[[0 for i in range(tLen+1)] for j in range(sLen+1)]
        for si in range(sLen+1):  
            d[si][0] = si  
        for ti in range(tLen+1):  
            d[0][ti] = ti
        for si in range(1,sLen+1):  
            ch1 = s[si-1]  
            for ti in range(1,tLen+1):
                ch2 = t[ti-1]
                if(ch1 == ch2):  
                    cost = 0
                else:  
                    cost = 1  
                d[si][ti] = min(min(d[si-1][ti]+1, d[si][ti-1]+1),d[si-1][ti-1]+cost)
        return d[sLen][tLen]
    
    def textsimilarity(self, src, tar):  
            ld = self.LevenshteinDistance(src, tar)
            return 1 - float(ld) / max(len(src), len(tar))
        
    def textsimilarity2(self, tar_list, src_list,abb_map):
        for i in range(len(tar_list)):
            if(tar_list[i] in abb_map):
                tar_list[i]=abb_map.get(tar_list[i])
        for i in range(len(src_list)):
            if(src_list[i] in abb_map):
                src_list[i]=abb_map.get(src_list[i])
        tar=tar_list[0]
        for i in range(1,len(tar_list)):
            tar=tar+"_"+tar_list[i]
        src=src_list[0]
        for i in range(1,len(src_list)):
            src=src+"_"+src_list[i]
        sim_total=self.textsimilarity(src, tar)
        return sim_total

class SemanticSimilarity:
    modelpath ="addressmodel_D1.model"
    model = Word2Vec.load(modelpath)
    
    def isexist(self, a):
        if a in self.model:
            return ("true")
        else:
            return ("false")
    
    def ussimilar(self,a,b):
        s=self.model.wv.similarity(a,b)
        return (s)
    
    def  src2tar_w2v( self, label_tar, label_src, abb_map):
        sim_src=0
        line_tar=self.isexist(label_tar)
        if(line_tar=="true"):
            line_src=self.isexist(label_src)
            if(line_src=="true"):
                line_sim=self.ussimilar(label_tar,label_src)
                if(float(line_sim)<0):
                    sim_src=0
                else:
                    sim_src=float(line_sim)
            elif(label_src in abb_map):
                label_src=abb_map.get(label_src)
                line_src=self.isexist(label_src)
                if(line_src=="true"):
                    line_sim=self.ussimilar(label_tar,label_src)
                    if(float(line_sim)<0):
                        sim_src=0
                    else:
                        sim_src=float(line_sim)
        elif(label_tar in abb_map):
            label_tar=abb_map.get(label_tar)
            line_tar=self.isexist(label_tar)
            if(line_tar=="true"):
                line_src=self.isexist(label_src)
                if(line_src=="true"):
                    line_sim=self.ussimilar(label_tar,label_src)
                    if(float(line_sim)<0):
                        sim_src=0
                    else:
                        sim_src=float(line_sim)
                elif(label_src in abb_map):
                    line_src=self.isexist(label_src)
                    if(line_src=="true"):
                        line_sim=self.ussimilar(label_tar,label_src)
                        if(float(line_sim)<0):
                            sim_src=0
                        else:
                            sim_src=float(line_sim)
        return sim_src
    
    def  srclist2tarlist_w2v(self, tar_list,src_list,abb_map):
        semsim_total=0
        min_list=[]
        max_list=[]
        min_list=tar_list
        max_list=src_list
        for i in range(len(min_list)): 
            sim_tar=0
            for j in range(len(max_list)):
                sim_src=0
                if(min_list[i]!="" and max_list[j]!=""):
                    sim_src=self.src2tar_w2v( min_list[i],max_list[j], abb_map)
                    if(sim_tar<sim_src):
                        sim_tar=sim_src
            semsim_total=semsim_total+sim_tar
        return  semsim_total/max(len(min_list),len(max_list))

    def odd(self, tar, src):
        if (tar % 2 == 0 and src % 2 == 0):
            return 0
        elif (tar % 2 == 1 and src % 2 == 1):
            return 0
        else:
            return 10

    def spatialsim(self, tar_list, src_list):
        spatialsim = 0
        min_list = tar_list
        max_list = src_list
        for i in range(0, len(min_list)):
            for j in range(0, len(max_list)):
                if (min_list[i] != "" and max_list[j] != ""):
                    tar_str = min_list[i].isdigit()
                    src_str_list = max_list[j].split("-")
                    for n in range(0, len(src_str_list)):
                        src_str = src_str_list[n].isdigit()
                        if (tar_str and src_str):
                            num_tar = int(min_list[i])
                            num_src = int(src_str_list[n])
                            o = self.odd(num_tar, num_src)
                            sim = float(num_tar) / (num_tar + abs(num_tar - num_src) + o)
                            if (spatialsim < sim):
                                spatialsim = sim
            return spatialsim


class SimilarObject: 
    name=""
    similarity=0
    def __init__(self, name, similarity):
            self.name = name
            self.similarity = similarity

    
def SimilarityW2v(tar,src,tar_list,src_list,abb_map,list):
    ss=SemanticSimilarity()
    
    for i in range(len(tar_list)):
        if(tar_list[i] in abb_map):
            tar_list[i]=abb_map.get(tar_list[i])
    for i in range(len(src_list)):
        if(src_list[i] in abb_map):
            src_list[i]=abb_map.get(src_list[i])
    tarnew=tar_list[0]
    for i in range(1,len(tar_list)):
        tarnew=tarnew+"_"+tar_list[i]
    srcnew=src_list[0]
    for i in range(1,len(src_list)):
        srcnew=srcnew+"_"+src_list[i]
    
    semsim_pname=0
    if(tarnew==srcnew):
        semsim_pname=1
    else:
        semsim_pname=ss.srclist2tarlist_w2v( tar_list, src_list,abb_map)
   
    so=SimilarObject(src,semsim_pname)
    list.append(so)


def SimilarityStringW2v(tar,src,tar_list,src_list,abb_map,list):
    ts=TextSimilarity()
    ss=SemanticSimilarity()
    textsim_pname=ts.textsimilarity2(tar_list, src_list,abb_map)
    semsim_pname=ss.srclist2tarlist_w2v( tar_list, src_list,abb_map)
    
    sim_total=0
    if(textsim_pname==1):
        sim_total=textsim_pname
    else:
        sim_total=textsim_pname*0.5+semsim_pname*0.5
    so=SimilarObject(src,sim_total)
    list.append(so)


def SimilarityStringW2vSpatial(tar, src, tar_list, src_list, abb_map, list):
    ts = TextSimilarity()
    ss = SemanticSimilarity()
    textsim_pname = ts.textsimilarity2(tar_list, src_list, abb_map)
    semsim_pname = ss.srclist2tarlist_w2v(tar_list, src_list, abb_map)
    spasim_pname = ss.spatialsim(tar_list, src_list)

    sim_total = 0
    if (textsim_pname == 1):
        sim_total = textsim_pname
    else:
        sim_total = textsim_pname * 1.0/3 + semsim_pname * 1.0/3 + spasim_pname * 1.0/3
    so = SimilarObject(src, sim_total)
    list.append(so)


def selectSort(list):
    for i in range(len(list)):
        k = i
        for j in range(len(list)-1,i,-1):
            if(list[j].similarity>list[k].similarity):
                k=j
        temp=list[i]
        list[i]=list[k]
        list[k]=temp

def selectMax(list):
    maxlist=[]
    sim_max=0
    for i in range(len(list)):
        if list[i].similarity == max and len(maxlist)> 0:
            maxlist.append(list[i])
        elif(list[i].similarity>sim_max):
            maxlist.clear()
            sim_max=list[i].similarity
            maxlist.append(list[i])
    return maxlist

def AddressMatch(address):
    country_bestlist=[]    
    city_bestlist=[]    
    street_bestlist=[]
    doorplate_bestlist=[]
    oc=OracleConnection()
    cursor=oc.getConnection()
    sql = "select  *  from ABB"
    rs=oc.SelectData(cursor, sql)
    abb=[]
    full=[]
    abb_map={}
    for row in rs:
        abb.append(row[0])
        full.append(row[1])
        abb_map=dict(zip(abb,full))

    poi_addresslist=address.split(" ")
    poi_country=poi_addresslist[3].strip("\n")
    poi_city=poi_addresslist[2]
    poi_street=poi_addresslist[1]
    poi_address=poi_addresslist[0]+"_"+poi_street+"_"+poi_city+"_"+poi_country
            
    tar_address = poi_address.split("_")
    tar_country = poi_country.split("_")
    tar_city = poi_city.split("_")
    tar_street = poi_street.split("_")
    
    country_bestlist.clear()
    sql = "select  *  from COUNTRY"
    rs=oc.SelectData(cursor, sql)
    for row in rs:
        country=row[0]
        src_country=country.split("_")
        SimilarityW2v(poi_country,country,tar_country,src_country,abb_map,country_bestlist)
    bestcountry = selectMax(country_bestlist)

    city_bestlist.clear()
    sql = "select  *  from CITY"
    rs=oc.SelectData(cursor, sql)
    for row in rs:
        city=row[0]
        src_city=city.split("_")
        SimilarityW2v(poi_city,city,tar_city,src_city,abb_map,city_bestlist)
    bestcity = selectMax(city_bestlist)

    street_bestlist.clear()
    sql = "select  *  from STREET"
    rs=oc.SelectData(cursor, sql)
    for row in rs:
        street=row[0]
        src_street=street.split("_")
        SimilarityW2v(poi_street,street,tar_street,src_street,abb_map,street_bestlist)
    beststreet = selectMax(street_bestlist)

    doorplate_bestlist.clear()
    for m_k in range(0, len(bestcountry)):
        for m_i in range(0, len(bestcity)):
            for m_j in range(0, len(beststreet)):
                best_country = bestcountry[m_k].name
                best_city = bestcity[m_i].name
                best_street = beststreet[m_j].name
                sql = " SELECT y FROM TABLE(SEM_MATCH( '{?y <http://www.poi.org/poi#hasCountry> <http://www.poi.org/poi#country/" + best_country + ">. ?y <http://www.poi.org/poi#hasCity> <http://www.poi.org/poi#city/" + best_city + ">. ?y <http://www.poi.org/poi#hasStreet> <http://www.poi.org/poi#street/" + best_street + ">}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))"
                rs = oc.SelectData(cursor, sql)
                for row in rs:
                    address_uri = row[0]
                    address_uri_list = address_uri.split("/")
                    address = address_uri_list[len(address_uri_list) - 1]
                    addresslist = address.split("_")
                    SimilarityW2v(poi_address, address, tar_address, addresslist, abb_map, doorplate_bestlist)

    selectSort(doorplate_bestlist)

    if(len(doorplate_bestlist)>0):
        file.writeFile(poi_address,"w2v_D1.txt")
        for j in range(len(doorplate_bestlist)):
            addresses="Match: "+ doorplate_bestlist[j].name+" "+str(doorplate_bestlist[j].similarity)
            file.writeFile(addresses,"w2v_D1.txt")
    else:
        for m_k in range(0, len(bestcountry)):
            for m_j in range(0, len(beststreet)):
                best_country = bestcountry[m_k].name
                best_street = beststreet[m_j].name
                sql = " SELECT y FROM TABLE(SEM_MATCH( '{?y <http://www.poi.org/poi#hasCountry> <http://www.poi.org/poi#country/" + best_country + ">. ?y <http://www.poi.org/poi#hasStreet> <http://www.poi.org/poi#street/" + best_street + ">}',SEM_Models('poi'), null, SEM_ALIASES(SEM_ALIAS('http://www.poi.org/','')), null))"
                rs = oc.SelectData(cursor, sql)
                for row in rs:
                    address_uri = row[0]
                    address_uri_list = address_uri.split("/")
                    address = address_uri_list[len(address_uri_list) - 1]
                    addresslist = address.split("_")
                    SimilarityW2v(poi_address, address, tar_address, addresslist, abb_map, doorplate_bestlist)
        selectSort(doorplate_bestlist)
        file.writeFile(poi_address,"w2v_D1.txt")
        for j in range(len(doorplate_bestlist)):
            addresses="Match: "+ doorplate_bestlist[j].name+" "+str(doorplate_bestlist[j].similarity)
            file.writeFile(addresses,"w2v_D1.txt")
    
    cursor.close()



file = FileOperator()
reference_address=file.readURI("matchedaddress_D1.txt")
for i in range(0, len(reference_address)):
    AddressMatch(reference_address[i])