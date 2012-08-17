;--------------------------------
;Includes stuff for Sections
  !include "Sections.nsh"
 
;--------------------------------
;Reserve Files
 
  ;Only useful for BZIP2 compression
 
 
  ReserveFile "jre.ini"
  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
 
;--------------------------------
;Installer Sections
 
Section -installjre jre
  Push $0
  Push $1
 
;  MessageBox MB_OK "Inside JRE Section"
  Strcmp $InstallJRE "yes" InstallJRE JREPathStorage
  DetailPrint "Starting the JRE installation"
InstallJRE:
  File /oname=$TEMP\jre_setup.exe j2re-setup.exe
  MessageBox MB_OK "Installing JRE"
  DetailPrint "Launching JRE setup"
  ;ExecWait "$TEMP\jre_setup.exe /S" $0
  ; The silent install /S does not work for installing the JRE, sun has documentation on the 
  ; parameters needed.  I spent about 2 hours hammering my head against the table until it worked
  ExecWait '"$TEMP\jre_setup.exe" /v\"/qn REBOOT=Suppress JAVAUPDATE=0 WEBSTARTICON=0\"' $0
  DetailPrint "Setup finished"
  Delete "$TEMP\jre_setup.exe"
  StrCmp $0 "0" InstallVerif 0
  Push "The JRE setup has been abnormally interrupted."
  Goto ExitInstallJRE
 
InstallVerif:
  DetailPrint "Checking the JRE Setup's outcome"
;  MessageBox MB_OK "Checking JRE outcome"
  Push "${JRE_VERSION}"
  Call DetectJRE  
  Pop $0	  ; DetectJRE's return value
  StrCmp $0 "0" ExitInstallJRE 0
  StrCmp $0 "-1" ExitInstallJRE 0
  Goto JavaExeVerif
  Push "The JRE setup failed"
  Goto ExitInstallJRE
 
JavaExeVerif:
  IfFileExists $0 JREPathStorage 0
  Push "The following file : $0, cannot be found."
  Goto ExitInstallJRE
  
JREPathStorage:
;  MessageBox MB_OK "Path Storage"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "UserDefinedSection" "JREPath" $1
  StrCpy $JREPath $0
  Goto End
  
ExitInstallJRE:
  Pop $1
  MessageBox MB_OK "The setup is about to be interrupted for the following reason : $1"
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0
SectionEnd
