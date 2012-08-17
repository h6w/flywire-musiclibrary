Bentokit Flywire v0.2.0 README.txt
Updated 27th February, 2009 by Ken McLean

This directory contains builds of Flywire 0.2.0, which are 
contributed by the Bentokit Project team. Please send all problems, 
comments or queries to info@bentokit.org 

http://bentokit.org/


    /*****************
     * ABOUT FLYWIRE *
     *****************/

Bentokit Flywire is an intuitive open-source radio station playout 
application and client. It is developed in Java based on the Java 
Media Framework for cross-platform use under Windows, Mac or Linux. 
It is primarily designed for stations in the community broadcasting 
sector who have a high turnover of volunteers and can't administer 
thorough training. 

Flywire takes sound files from a set of subdirectories and displays 
them in various lists on the screen. The user then selects items from 
these lists to construct a playlist, which can be played when ready. 
When used as a client in conjunction with the Retromod Project, it 
has automated updating functions for Station IDs, Promos, Show Media, 
and scheduled Sponsorship Announcements, and also has an automatic 
playout function for when the studio is unattended. While Flywire can 
be used as a standalone application, it very much benefits from an 
administration interface such as Retromod.


    /*************************
     * PREREQUISITE SOFTWARE *
     *************************/

Flywire is a Java application, and requires the Java Runtime 
Environment (JRE 1.6 and higher), and the Java Media Framework (JMF) 
in order to run. 

Both can be obtained at: http://java.com/


    /**********************
     * INSTALLING FLYWIRE *
     **********************/

Just unpack by running the installer wizard. It will create desktop 
and start menu shortcuts by default.


    /***********************
     * DIRECTORY STRUCTURE *
     ***********************/

    ./AutoMedia/        Contains the MUSIC/ directory;
    ./AutoMedia/MUSIC/  Repository for media used during Automatic DJ 
                        Mode playback;
    ./Log/              Repository for log files generated after 
                        playback of scheduled media;
    ./Schedule/         Repository for schedule placeholder files used 
                        for scheduled media playback. These will appear
                        under Sponsorship Announcements at the next 
                        alloted time;
    ./ScheduledMedia/   Repository for the scheduled media referenced 
                        by the schedule files;
    ./ShowMedia/        Contains the program directories for Show Media
                        playback. Place directories in here containing 
                        media to select them from the dropdown menu, 
                        to display a list under Show Media;
    ./StationMedia/     Repository for station media, eg. IDs and 
                        Promos. This displays in the Station Media 
                        list;


    /************
     * SCHEDULE *
     ************/

The schedule is built as a series of discrete 0 byte placeholder 
files stored in the ./Schedule/ directory, with the naming 
nomenclature of "<timestamp>;<filename>", with the timestamp and 
filename as semi-colon delimited values.
    eg. "2009-02-23 13-55-00;SPONSOR - Noodle Emporium.mp3"

The timestamp should be in the following format:
    "YYYY-MM-DD hh-mm-ss"
    eg. "2009-02-23 13-55-00"

The filename should represent the filename of the desired media as 
listed in the ./ScheduledMedia/ directory.
    eg. "SPONSOR - Noodle Emporium.mp3"


    /*************
     * LOG FILES *
     *************/
    
Once a scheduled media file has been successfully played, Flywire 
will move the corresponding schedule file to the ./Log/ directory, 
renaming it with the ".played" extension, and writing a timestamp 
of the time it was played into the file.
    eg. "2009-02-23 11-55-00;SPONSOR - Noodle Emporium.mp3.played"
        containing the text: "2009-02-23 11-53-23"


    /*************
     * SHORTCUTS *
     *************/

    P = Play            Hotwiring a keyboard's P button to a relay 
                        allows remote start of the application.