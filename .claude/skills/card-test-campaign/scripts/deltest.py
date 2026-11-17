#!/usr/bin/env python3
"""deltest.py <file> <method>...: remove whole test methods (with their annotations and the comment lines
directly above them) from a Java test file. Prints what it removed."""
import sys,re
f=sys.argv[1]; L=open(f).read().split('\n')
for name in sys.argv[2:]:
    idx=[i for i,l in enumerate(L) if re.search(r'\bpublic void '+re.escape(name)+r'\s*\(',l)]
    assert len(idx)==1,(name,idx)
    i=idx[0]; start=i
    while start>0 and (L[start-1].strip().startswith('@') or L[start-1].strip().startswith('//')): start-=1
    indent=re.match(r'\s*',L[i]).group(0)
    end=i
    while not (L[end].rstrip()==indent+'}'): end+=1
    while start>0 and L[start-1].strip()=='' and end+1<len(L) and L[end+1].strip()=='': start-=1
    print('removed',name,'lines',start+1,'-',end+1)
    del L[start:end+1]
s='\n'.join(L)
if '@Ignore' not in s: s=s.replace('import org.junit.Ignore;\n','')
open(f,'w').write(s)
