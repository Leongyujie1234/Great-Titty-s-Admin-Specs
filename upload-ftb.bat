@echo off
cd /d "C:\Users\great generos\Downloads\mods"
echo cd mods > psftp-ftb.txt
echo put ftb-essentials-neoforge-2101.1.8.jar >> psftp-ftb.txt
echo put ftb-library-neoforge-2101.1.32.jar >> psftp-ftb.txt
echo put ftb-teams-neoforge-2101.1.10.jar >> psftp-ftb.txt
echo architectury-13.0.8-neoforge.jar already on server >> psftp-ftb.txt
echo quit >> psftp-ftb.txt
psftp cy3yogsv.f2211c9c@silkroad.ultraservers.com:60002 -l cy3yogsv.f2211c9c -pw f3XIXN;=Urt) -hostkey "ssh-ed25519 255 SHA256:Xdr9j/YYIcn/KUxolxtjFKec+xFwFBIL/jZ/LGrIf9U" -b psftp-ftb.txt
