# BTCFG-II
Configuration software for the P25RX-II (non-tree-view / non-dBase version)

<B>Acquiring and Building From Source</B>

Get Latest BTConfig source :
git clone https://github.com/bluetailtech/BTCFG-II.git

1) Install Oracle Java 1.8  (or greater)

2) Install Netbeans 8.1   
(older version of Netbeans. Not susceptable to the same security issues that some newer versions are )
  
  Dowload Netbeans Installer from https://netbeans.org/downloads/old/8.1/
    Excecute Netbeans installer.
   Linux :
      chmod +x netbeans-8.1-linux.sh
    ./netbeans-8.1-linux.sh
   Windows :
      netbeans-8.1-javase-windows.exe

3) Start netbeans and open the project BTConfig/Source Packages/btconfig/BTFrame.java

4) In the Netbeans editor :  select  then "Run Project". This will build and execute the 
resulting BTConfig.jar. The file will be in BTConfig/dist

5) Now BTConfig.exe can be built from inside the BTConfig directory with 'sh build.sh' or 'ant exe' 
(note: you may be able to skip steps 2-4 for a build-only)
