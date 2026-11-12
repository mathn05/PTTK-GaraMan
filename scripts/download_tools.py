from pathlib import Path
import hashlib, urllib.request, zipfile

ROOT=Path(__file__).resolve().parents[1]
base=ROOT/'.runtime'
(base/'downloads').mkdir(parents=True,exist_ok=True)
for name,url in [('apache-maven-3.10.0','https://dlcdn.apache.org/maven/maven-3/3.10.0/binaries/apache-maven-3.10.0-bin.zip'),('apache-tomcat-10.1.61','https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.61/bin/apache-tomcat-10.1.61.zip')]:
    if (base/name).exists():continue
    dest=base/'downloads'/url.rsplit('/',1)[1]
    if not dest.exists():urllib.request.urlretrieve(url,dest)
    checksum=urllib.request.urlopen(url+'.sha512').read().decode().split()[0].lower()
    if hashlib.sha512(dest.read_bytes()).hexdigest()!=checksum:raise RuntimeError('Checksum mismatch: '+name)
    with zipfile.ZipFile(dest) as z:z.extractall(base)
    print(name,'SHA512 verified')
