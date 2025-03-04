
## Version History

Version 3.5 (Mar 05, 2025)

- 	Use jenkins.baseline in pom.xml
-	PR#10 Upgrade to latest LTS core version supporting Java 8 for pom.xml

   
Version 3.4 (Oct 25, 2024)

-       POM update for jenkins.version = 2.414.3
-		Replaced inline event handler for select all checkboxes


Version 3.3 (Feb 08, 2024)

-       Fixed issue with Manage Jenkins icon for Pending Request icon not found - JENKINS-72673
-       Fixed issue with null values - JENKINS-72572
-       Fixed issue with select all requests in Pending Request page

Version 3.2 (Sept 29, 2023)

-       Dependabot pom dependency updates for security issues
-		Pom dependency update required minimum jenkins version 2.387.3
-		Fix nullexception with string, from v 3.0 rework


Version 3.1 (August 11, 2023)

-       Remove unnecessary javascript from jelly files
-		Fixed username in permission error message


Version 3.0 (June 20, 2023)

-       Rework Request object to better support Folders and Multibranch Pipeline Job types that affect all request types
		This provides full job names in the Pending Requests web page and correct links to be processed


Version 2.2.23 (May 29, 2023)

-       Fixed breakage in Delete/Unlock Build Requests links


Version 2.2.22 (May 17, 2023)

-       Fixed Delete/Unlock builds Requests if a Folder is given a Display name


Version 2.2.21 (May 10, 2023)

-		Fixed missing parameters from jenkins jelly script (caused requests to get discarded instead of applied) 


Version 2.2.20 (April 14, 2023)

-       Fix Delete Builds and Unlocks Builds link issues when set build name was applied


Version 2.2.19 (September 26, 2022)

-       Update POM dependencies
-       Fix wrong user name in permission error messages
-		Add new Request to Delete Multibranch Pipeline
-		Moved Pending Requests - Manage Jenkins link from Uncategorized to Tools and Actions


Version 2.2.18 (May 19, 2022)

-		Fix issue with emails not sending
-		Partial changes to adapt icon path removal from core       


Version 2.2.17 (March 16, 2022)

-       Fix for SECURITY-2650


Version 2.2.16 (March 07, 2022)

-       Update POM dependencies
-		Improve error return messages
-		Changes to comply with latest Spotbugs


Version 2.2.15 (September 02, 2021)

-       Fix path issue with Delete Build and Unlock Build requests


Version 2.2.14 (June 22, 2021)

-       Correct permissions on Rename so links appear


Version 2.2.13 (June 21, 2021)

-       Fix index.jelly layout issue


Version 2.2.12 (June 18, 2021)

-       Fix path issue with Delete Build and Unlock Build requests


Version 2.2.11 (June 08, 2021)

-       Security fix: Bumped httpclient from 4.3.6 to 4.5.13.
-       Security fix: Bumped httpcore from 4.4.3 to 4.4.13.


Version 2.2.10 (June 01, 2021)

-       Fix issue with spaces in names

Version 2.2.9 (November 16, 2020)

-       Fix issue with Folders related to encoding from previous release

Version 2.2.8 (November 04, 2020)

- 		Changes to address issues with SECURITY-2136
-		Fix project names that require encoding for POST urls

Version 2.2.7 (October 07, 2020)

-       Fix Post error on Process Requests that started with Jenkins 2.249.1 for CSRF changes
-       Fix for test email address
-       Dependency updates

Version 2.2.6 (February 12, 2020)

-	Add New Delete Folder request [JIRA] (JENKINS-61011)
-	Add New Rename Folder request [JIRA] (JENKINS-61011)

Version 2.2.5 (January 24, 2020)

-	Add multiple email support to test email configuration.

Version 2.2.4 (January 16, 2020)

-	JENKINS-60780 Include change for Delete Build, Rename Job, Unlock Build along with Delete Job.
-	Add Email support to Rename requests.
-	Remove multiple emails on the same request.

Version 2.2.3 (January 16, 2020)

-	JENKINS-60780 Additional change for this issue.

Version 2.2.2 (January 15, 2020)

-	JENKINS-60780 Fixed delete job when within multiple folders views.
-	JENKINS-60781 Allow multiple admin email addresses.

Version 2.2.1 (January 08, 2020)

-	Changed from user password for Delete/Unlock requests to a jenkins user token.
-	Delete build will now unlock before deleting if its locked instead of warning of locked build.

Version 2.0.5 (May 30, 2019)

-   Added Rename Job support back

Version 2.0.4 (May 29, 2019)

-   Added support for Pipeline jobs and builds

Version 2.0.3 (May 28, 2019)

-   Fixed the Creation date format in the Pending request page

Version 2.0.2 (May 24, 2019)

-   Initial release

