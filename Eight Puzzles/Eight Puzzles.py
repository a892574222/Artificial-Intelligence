# -*- coding: UTF-8 -*- 
import os
import time
def show(i):
            print("-->",i[:3])
            print("-->",i[3:6])
            print("-->",i[6:9])
            print('-------------------------------')

class Node(object):
    """初始化一个节点,需要为节点设置值"""
    def __init__(self, val):
        self.step=0
        self.val = val
        self.father = None
        self.f=0
class BinaryTree(object):
    def __init__(self,startpoiont):
        self.target ='123456780'
        self.place = 0
        self.open=[]
        self.close=[]
        self.startpoint=startpoiont
    def searchnear(self,minF,x,y):
        #空格越界
        if (self.place<3 and y==-1) or (self.place>5 and y==1):
            return
        elif (self.place==0 or self.place==3 or self.place==6)and x==-1:
            return
        elif (self.place==2 or self.place==5 or self.place==8)and x==1:
            return
        current = self.change(minF,x,y)#字符串
        if self.checkinend(current):
            return
        if self.checkinopen(current):
            acurrent=self.inopenclass(current)#返回类
            if  minF.step+1+self.guess(acurrent.val)<acurrent.f:
                acurrent.step=minF.step+1
                acurrent.f= acurrent.step+self.guess(acurrent.val)
                acurrent.father=minF
                
                return
        else:
            
            acurrent=Node(current)
            acurrent.father=minF
            acurrent.step=minF.step+1
            acurrent.f=acurrent.step+self.guess(acurrent.val)
            self.open.append(acurrent)
            return
        
    def checkinend(self,a):
        for i in self.close:
            if a==i.val:
                return True
        return False
    
    def inopenclass(self,a):
                for i in self.open:
                    if a==i.val:
                        return i

    def checkinopen(self,a):
        for i in self.open:
            if a==i.val:
                return True
        return False
        
    def change(self,a,x,y):
        #根据字符串a交换字符串中0与另一个字符的顺序
        if x==1:
            b=a.val+'*'
            if b[self.place+2]!='*':
                b=a.val[:self.place]+a.val[self.place+1]+str(0)+a.val[self.place+2:]
            else:
                b=a.val[:self.place]+a.val[self.place+1]+str(0)
            return b
        elif x==-1:
            b=a.val[:self.place-1]+str(0)+a.val[self.place-1]+a.val[self.place+1:]
            return b
        elif y==1:
            b=a.val[:self.place]+a.val[self.place+3]+a.val[self.place+1:self.place+3]+str(0)+a.val[self.place+4:]
            return b
        elif y==-1:
            if self.place==8:
                b=a.val[:self.place-3]+str(0)+a.val[self.place-2:self.place]+a.val[self.place-3]
            else:
                b=a.val[:self.place-3]+str(0)+a.val[self.place-2:self.place]+a.val[self.place-3]+a.val[self.place+1:]
            return b
        
    def guess(self,a):
        h=0
        b=[[a[0],a[1],a[2]],[a[3],a[4],a[5]],[a[6],a[7],a[8]]]
        c=[['1','2','3'],['4','5','6'],['7','8','0']]
        for i in range(3):
           for j in range(3):
               for x in range(3):
                   for y in range(3):
                        if c[i][j]==b[x][y]:
                           h=h+abs(x+y-i-j)

        #另外一种求h(n)的方法
        #for i in range(9):
        #    if a[i]!=self.target[i]:
        #        h+=1

        return h


    def check(self):
        for i in self.open:
            if i.val ==self.target:
                print('最少步数为:'+str(i.step))
                print('步骤如下:')
                return i
        return None


    
    def getminnode(self):
        a=self.open[0]
        for i in self.open:
            if i.f<a.f:
                a=i
        return a

    
    def start(self):
        #创立根节点类
        d=Node(self.startpoint)
        #把根节点放入open表
        self.open.append(d)
        while len(self.open):
            #print(len(self.open))
            #寻找open表中估价函数值最小的点的类
            minF = self.getminnode()
            self.place=minF.val.index('0')
            self.open.remove(minF)
            self.close.append(minF)
            #判断该节点的上下左右结点
            self.searchnear(minF, 0, -1)
            self.searchnear(minF, 0, 1)
            self.searchnear(minF, -1, 0)
            self.searchnear(minF, 1, 0)
            judge = self.check()
            if judge:
                bjudge=judge
                path=[]
                while True:
                    if bjudge.father:
                        path.append(bjudge.val)
                        bjudge=bjudge.father
                    else:
                        return list(reversed(path))
            if len(self.open) == 0:
                return None
def status(a):
    b=0
    for i in range(9):
        for j in range(i):
            if a[i]>a[j]and a[j]!='0':
                b+=1
    #print(b)
    b=b%2+1
    return b
def main():
    input('分三次输入用0代表空格,回车后请输入第一行')
    a=input()
    b=input()
    c=input()
    d=a+b+c
    time_start=time.time()
    if d=='123456780':
        print('输入已经为答案')
    else:
        if status(d)==1:
            bt = BinaryTree(d)
            e=bt.start()
            if e!=None:
                for i in e:
                    show(i)
        else:
            print("无解")
    time_end=time.time()
    print('耗时:',str('%.2f'%(time_end-time_start)))
if __name__ == '__main__':
    main()
    os.system('pause')
