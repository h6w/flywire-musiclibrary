; flywire.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install flywire.nsi into a directory that the user selects,

!define AppName "Flywire"
!define AppVersion "0.2"
!define ShortName "flywire"
!define JRE_VERSION "1.6.2"
!define Vendor "Bentokit Project"

!ifndef VERSION
!warning "VERSION not defined."
!define VERSION 0
!endif


;--------------------------------
;Include Modern UI

  !include "MUI.nsh"


;--------------------------------
;Include Java Detection

;  !include "java_require.nsi"

;--------------------------------

; The name of the installer
Name "Flywire"

; The file to write
OutFile "Flywire-${RELEASETYPE}-Windows-x86-${VERSION}.exe"

;Request application privileges for Windows Vista
RequestExecutionLevel highest

; The default installation directory
InstallDir $PROGRAMFILES\Bentokit\Flywire

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\Bentokit\Flywire" "Install_Dir"

;--------------------------------
;Interface Configuration
	; MUI Settings / Icons
	!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install-nsis.ico"
	!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall-nsis.ico"
	 
	; MUI Settings / Header
	!define MUI_HEADERIMAGE
	!define MUI_HEADERIMAGE_LEFT
	!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange.bmp"
	!define MUI_HEADERIMAGE_UNBITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-uninstall.bmp"
	 
	; MUI Settings / Wizard
	!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange.bmp"
	!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall.bmp"

;  !define MUI_ICON "flywire.ico"
;  !define MUI_UNICON "flywire.ico"
;  !define MUI_HEADERIMAGE
;  !define MUI_HEADERIMAGE_BITMAP "flywire.bmp" ; optional
  !define MUI_ABORTWARNING

!insertmacro MUI_LANGUAGE "English"



;--------------------------------
; INSTALL PAGES
 
  ; Welcome Page
  ;!insertmacro MUI_PAGE_WELCOME

  ; License page
  !define MUI_TEXT_LICENSE_TITLE "GNU General Public License v3"
  !insertmacro MUI_PAGE_LICENSE "LICENSE.txt"

  ; Ask the user to select the components to install
  !insertmacro MUI_PAGE_COMPONENTS

  ; Ask the user to select an install directory
  !insertmacro MUI_PAGE_DIRECTORY

  ; Install the files
  !insertmacro MUI_PAGE_INSTFILES

  ; Finish Page
  ;!insertmacro MUI_PAGE_FINISH

;--------------------------------
; UNINSTALL PAGES

  ; Ask the user to confirm uninstalling
  !insertmacro MUI_UNPAGE_CONFIRM

  ; Uninstall the files
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------

; The stuff to install
Section "Flywire (required)"
  SetShellVarContext all
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR

  ;ADD YOUR OWN FILES HERE...  
  SetOverwrite on

  ; Put file there
  File /r "Flywire.jar"

  SetOutPath "$INSTDIR\bin"
  File /r "bin\logo.png"
  File /r "bin\play.png"
  File /r "bin\playdisabled.png"
  File /r "bin\stop.png"
  File /r "bin\stopdisabled.png"

  File /r "LICENSE.txt"
  File /r "flywire.ico"

  ;SetBrandingImage "flywire_logo.bmp"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\Bentokit\Flywire "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Flywire" "DisplayName" "Flywire"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Flywire" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Flywire" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Flywire" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"
  SetShellVarContext all

  SetOutPath $INSTDIR
  CreateDirectory "$SMPROGRAMS\Bentokit\Flywire"
  CreateShortCut "$SMPROGRAMS\Bentokit\Flywire\Flywire.lnk" "javaw" "-Xmx300m -jar Flywire.jar" "$INSTDIR\flywire.ico" 0
  CreateShortCut "$SMPROGRAMS\Bentokit\Flywire\Flywire (Debug Mode).lnk" "java" "-Xmx300m -jar Flywire.jar --debug" "$INSTDIR\flywire.ico" 0
  CreateShortCut "$SMPROGRAMS\Bentokit\Flywire\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Flywire"
  DeleteRegKey HKLM SOFTWARE\Bentokit\Flywire

  ; Remove files and uninstaller
  Delete $INSTDIR\bin\*.*
  Delete $INSTDIR\*.*

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\Bentokit\Flywire\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\Bentokit\Flywire"
  RMDir "$INSTDIR\bin"
  RMDir "$INSTDIR"

SectionEnd
